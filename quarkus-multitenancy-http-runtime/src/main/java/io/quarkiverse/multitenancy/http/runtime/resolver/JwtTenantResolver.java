/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.multitenancy.http.runtime.resolver;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * Resolves the tenant from a verified JWT bearer token.
 *
 * <p>
 * The resolver delegates token verification to the SmallRye JWT extension by
 * injecting {@link JsonWebToken} — the same bean Quarkus uses for principal
 * propagation. By the time this filter runs (at {@code Priorities.AUTHENTICATION}),
 * Quarkus has already validated the token's signature and produced either:
 * <ul>
 * <li>A populated {@code JsonWebToken} (token verified successfully), or</li>
 * <li>A {@code NullJsonWebToken} (no token, or verification failed; anonymous
 * identity).</li>
 * </ul>
 *
 * <p>
 * Resolution semantics:
 * <ul>
 * <li>No {@code Authorization: Bearer …} header on the request →
 * {@link TenantResolution#notApplicable()} so the dispatcher moves on to
 * the next strategy.</li>
 * <li>Bearer token present but {@code JsonWebToken} is the null proxy (token
 * failed verification, or the principal was not a JWT) →
 * {@link TenantResolution#rejected(String)}. The dispatcher must abort with
 * 401 and must not fall back to {@link HttpTenantConfig#defaultTenant()}.</li>
 * <li>Bearer token verified but the configured claim is missing, non-string,
 * or blank → {@link TenantResolution#rejected(String)} for the same reason.</li>
 * <li>Verified token with a non-blank string claim →
 * {@link TenantResolution#resolved(String)}.</li>
 * </ul>
 *
 * <p>
 * This deliberately removes the previous base64-decode path. Operators must
 * configure SmallRye JWT verification (typically
 * {@code mp.jwt.verify.publickey.location} plus a pinned algorithm) for the
 * JWT strategy to function.
 */
@Priority(TenantResolver.DEFAULT_PRIORITY)
@ApplicationScoped
public class JwtTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(JwtTenantResolver.class);

    private static final String BEARER = "Bearer";

    @Inject
    HttpTenantConfig config;

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    @Override
    public String name() {
        return HttpTenantStrategy.jwt.name();
    }

    @Override
    public TenantResolution resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context available; JWT strategy not applicable");
            return TenantResolution.notApplicable();
        }

        BearerHeader bearer = classifyAuthorization(reqOpt.get().getHeaderString("Authorization"));
        switch (bearer) {
            case ABSENT -> {
                logger.debug("No bearer token on the request; JWT strategy not applicable");
                return TenantResolution.notApplicable();
            }
            case MALFORMED -> {
                logger.debug("Bearer scheme present but token is blank or malformed");
                return TenantResolution.rejected("Bearer token is blank or malformed");
            }
            case PRESENT -> {
                // fall through to verification check below
            }
        }

        // A bearer token was sent. Quarkus security has already attempted to
        // authenticate it. If the resulting identity is anonymous the token
        // either failed verification or was not a JWT — in both cases we
        // reject so the dispatcher cannot fall back to the default tenant.
        if (identity.isAnonymous()) {
            logger.debug("Bearer token present but request is anonymous; JWT authentication failed");
            return TenantResolution.rejected("Bearer token was not authenticated");
        }

        String claimName = config.jwtClaimName();
        Object rawClaim = jwt.getClaim(claimName);
        if (rawClaim == null) {
            logger.debugf("JWT verified but missing required tenant claim '%s'", claimName);
            return TenantResolution.rejected("JWT missing required tenant claim: " + claimName);
        }
        if (!(rawClaim instanceof String rawTenant)) {
            logger.debugf("JWT tenant claim '%s' is not a string (type=%s)",
                    claimName, rawClaim.getClass().getName());
            return TenantResolution.rejected("JWT tenant claim is not a string");
        }
        String tenant = rawTenant.trim();
        if (tenant.isBlank()) {
            logger.debugf("JWT tenant claim '%s' is blank", claimName);
            return TenantResolution.rejected("JWT tenant claim is blank");
        }

        logger.debugf("Tenant resolved from JWT claim '%s'", claimName);
        return TenantResolution.resolved(tenant);
    }

    /**
     * Classifies the {@code Authorization} header relative to the Bearer
     * scheme. HTTP auth scheme tokens are case-insensitive per RFC 7235
     * §2.1, so we match using {@link String#regionMatches(boolean, int, String, int, int)}.
     */
    private static BearerHeader classifyAuthorization(String header) {
        if (header == null || header.isBlank()) {
            return BearerHeader.ABSENT;
        }
        String trimmed = header.trim();
        if (!trimmed.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            return BearerHeader.ABSENT;
        }
        // The scheme matched. Anything past it must be a separator + non-blank
        // token, otherwise the header carries an empty or malformed Bearer.
        if (trimmed.length() == BEARER.length()
                || !Character.isWhitespace(trimmed.charAt(BEARER.length()))) {
            return BearerHeader.MALFORMED;
        }
        String token = trimmed.substring(BEARER.length() + 1).trim();
        return token.isEmpty() ? BearerHeader.MALFORMED : BearerHeader.PRESENT;
    }

    private enum BearerHeader {
        ABSENT,
        MALFORMED,
        PRESENT
    }
}
