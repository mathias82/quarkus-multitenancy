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
package io.quarkiverse.multitenancy.http.runtime.startup;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;
import io.quarkus.runtime.StartupEvent;

/**
 * Fails fast at startup when the JWT tenant strategy is active but the
 * application has not configured a way to verify incoming tokens.
 *
 * <p>
 * The check is intentionally conservative: it only triggers an exception when
 * {@link HttpTenantStrategy#jwt} is in {@link HttpTenantConfig#strategy()}
 * <em>and</em> neither of the two standard sources of a verified
 * {@code JsonWebToken} can be detected from configuration:
 * <ul>
 * <li>SmallRye JWT, signalled by {@code mp.jwt.verify.publickey.location} or
 * {@code mp.jwt.verify.publickey} being set.</li>
 * <li>OIDC, signalled by {@code quarkus.oidc.auth-server-url} (or the
 * tenant-specific variants) being set.</li>
 * </ul>
 *
 * <p>
 * Applications that wire {@code JsonWebToken} through a non-standard producer
 * may set {@code quarkus.multi-tenant.http.jwt.skip-startup-check=true} to opt
 * out and rely on the documented operator contract instead.
 */
@ApplicationScoped
public class JwtStrategyStartupValidator {

    private static final Logger logger = Logger.getLogger(JwtStrategyStartupValidator.class);

    private static final String SKIP_PROPERTY = "quarkus.multi-tenant.http.jwt.skip-startup-check";

    private static final List<String> SMALLRYE_JWT_HINTS = List.of(
            "mp.jwt.verify.publickey.location",
            "mp.jwt.verify.publickey");

    private static final String OIDC_DEFAULT_AUTH_SERVER_URL = "quarkus.oidc.auth-server-url";

    private static final String OIDC_NAMED_TENANT_PREFIX = "quarkus.oidc.";

    private static final String OIDC_AUTH_SERVER_URL_SUFFIX = ".auth-server-url";

    @Inject
    HttpTenantConfig config;

    @Inject
    Config mpConfig;

    void onStart(@Observes StartupEvent event) {
        if (!config.enabled()) {
            return;
        }
        if (!jwtStrategyEnabled()) {
            return;
        }
        if (skipRequested()) {
            logger.debugf("Skipping JWT startup check because %s=true", SKIP_PROPERTY);
            return;
        }
        if (hasVerifiedJsonWebTokenSource()) {
            return;
        }

        throw new IllegalStateException(
                "The 'jwt' tenant resolution strategy is enabled in "
                        + "quarkus.multi-tenant.http.strategy but no verified JsonWebToken source is configured. "
                        + "Configure SmallRye JWT (set 'mp.jwt.verify.publickey.location' or "
                        + "'mp.jwt.verify.publickey') or Quarkus OIDC (set 'quarkus.oidc.auth-server-url' "
                        + "or 'quarkus.oidc.<tenant>.auth-server-url'), remove 'jwt' from the strategy chain, "
                        + "or set '" + SKIP_PROPERTY + "=true' if you provide JsonWebToken via a custom producer.");
    }

    private boolean jwtStrategyEnabled() {
        for (String configured : config.strategy()) {
            if (configured == null) {
                continue;
            }
            if (HttpTenantStrategy.jwt.name().equalsIgnoreCase(configured.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean skipRequested() {
        return config.jwt().skipStartupCheck();
    }

    private boolean hasVerifiedJsonWebTokenSource() {
        return hasSmallryeJwtConfigured() || hasOidcConfigured();
    }

    private boolean hasSmallryeJwtConfigured() {
        for (String hint : SMALLRYE_JWT_HINTS) {
            if (hasNonBlank(hint)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOidcConfigured() {
        if (hasNonBlank(OIDC_DEFAULT_AUTH_SERVER_URL)) {
            return true;
        }
        // Iterate the resolved property names to pick up tenant-specific OIDC
        // configuration such as quarkus.oidc.<tenant>.auth-server-url.
        for (String propertyName : mpConfig.getPropertyNames()) {
            if (propertyName.startsWith(OIDC_NAMED_TENANT_PREFIX)
                    && propertyName.endsWith(OIDC_AUTH_SERVER_URL_SUFFIX)
                    && hasNonBlank(propertyName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNonBlank(String key) {
        Optional<String> value = mpConfig.getOptionalValue(key, String.class);
        return value.isPresent() && !value.get().isBlank();
    }
}
