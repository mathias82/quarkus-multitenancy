package io.quarkiverse.multitenancy.http.runtime.resolver;

import java.util.Optional;

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
@ApplicationScoped
public class JwtTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(JwtTenantResolver.class);

    @Inject
    HttpTenantConfig config;

    @Inject
    JsonWebToken jwt;

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

        String authHeader = reqOpt.get().getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No bearer token on the request; JWT strategy not applicable");
            return TenantResolution.notApplicable();
        }

        // A bearer token was sent. SmallRye JWT has already attempted to
        // verify it; an unpopulated principal name signals either a failed
        // verification or a non-JWT credential injected via the null proxy.
        if (jwt.getName() == null) {
            logger.debug("Bearer token present but JsonWebToken is unpopulated (verification failed)");
            return TenantResolution.rejected("JWT verification failed");
        }

        String claimName = config.jwtClaimName();
        Object rawClaim = jwt.getClaim(claimName);
        if (rawClaim == null) {
            logger.debugf("JWT verified but missing required tenant claim '%s'", claimName);
            return TenantResolution.rejected("JWT missing required tenant claim: " + claimName);
        }
        if (!(rawClaim instanceof String tenant)) {
            logger.debugf("JWT tenant claim '%s' is not a string (type=%s)",
                    claimName, rawClaim.getClass().getName());
            return TenantResolution.rejected("JWT tenant claim is not a string");
        }
        if (tenant.isBlank()) {
            logger.debugf("JWT tenant claim '%s' is blank", claimName);
            return TenantResolution.rejected("JWT tenant claim is blank");
        }

        logger.debugf("Resolved tenant '%s' from JWT claim '%s'", tenant, claimName);
        return TenantResolution.resolved(tenant);
    }
}
