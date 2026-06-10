package io.quarkiverse.multitenancy.http.runtime.startup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;
import io.quarkus.runtime.StartupEvent;

/**
 * Logs a warning at startup when the {@code jwt} strategy is active because of
 * the implicit default — i.e. the application never set
 * {@code quarkus.multi-tenant.http.strategy} explicitly and is relying on the
 * built-in {@code header,jwt,cookie} chain.
 *
 * <p>
 * This is step 1 of the deprecation cycle for changing the default chain to
 * {@code header,cookie} (tracked in issue #15). After #14 the {@code jwt}
 * strategy fails fast at startup without a verified-token source, which is the
 * correct behaviour but also a noisier upgrade than necessary for applications
 * that only intended to use header- or cookie-based resolution and never opted
 * into JWT explicitly.
 *
 * <p>
 * The warning is intentionally narrow:
 *
 * <ul>
 * <li>It fires only when the extension itself is enabled and {@code jwt} is in
 * the effective strategy list, so applications that already drop {@code jwt}
 * see nothing.</li>
 * <li>It fires only when {@code quarkus.multi-tenant.http.strategy} does not
 * appear in any active SmallRye Config source. If the value is present — even
 * if it happens to match the historical default verbatim — the application is
 * treated as having made an active choice and no warning is emitted.</li>
 * </ul>
 *
 * <p>
 * The warning does not throw or change behaviour; it only signals the upcoming
 * default change so operators have a release cycle to react.
 */
@ApplicationScoped
public class ImplicitDefaultStrategyWarner {

    private static final Logger logger = Logger.getLogger(ImplicitDefaultStrategyWarner.class);

    static final String STRATEGY_PROPERTY = "quarkus.multi-tenant.http.strategy";

    @Inject
    HttpTenantConfig config;

    @Inject
    Config mpConfig;

    void onStart(@Observes StartupEvent event) {
        if (!config.enabled()) {
            return;
        }
        if (!jwtInStrategy()) {
            return;
        }
        if (strategyIsExplicit()) {
            return;
        }

        logger.warnf(
                "The HTTP multitenancy 'jwt' strategy is active because of the implicit default chain "
                        + "(%s=header,jwt,cookie). A future minor release will change the default to "
                        + "'header,cookie' so applications that never opted into JWT no longer need to "
                        + "configure JWT verification. To silence this warning, set %s explicitly — for "
                        + "example '%s=header,cookie' to opt out of JWT or '%s=header,jwt,cookie' to "
                        + "keep the current behaviour.",
                STRATEGY_PROPERTY, STRATEGY_PROPERTY, STRATEGY_PROPERTY, STRATEGY_PROPERTY);
    }

    private boolean jwtInStrategy() {
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

    /**
     * Returns {@code true} when {@code quarkus.multi-tenant.http.strategy} is provided
     * by a {@link org.eclipse.microprofile.config.spi.ConfigSource} that the user
     * controls (an application property file, environment variable, system property,
     * dev-services source, etc.) rather than by the {@code @WithDefault} fallback.
     *
     * <p>
     * Implementation note: SmallRye Config also exposes {@code @WithDefault} values
     * through a synthetic source named {@code "DefaultValuesConfigSource"} pinned at
     * {@link Integer#MIN_VALUE} ordinal, so naively iterating
     * {@link org.eclipse.microprofile.config.Config#getPropertyNames()} sees the
     * property even when the user never set it. The ordinal check below is the
     * portable way to filter those out — any explicit source has a higher ordinal.
     */
    private boolean strategyIsExplicit() {
        if (mpConfig instanceof io.smallrye.config.SmallRyeConfig sconfig) {
            io.smallrye.config.ConfigValue cv = sconfig.getConfigValue(STRATEGY_PROPERTY);
            return cv.getValue() != null && cv.getConfigSourceOrdinal() > Integer.MIN_VALUE;
        }
        return false;
    }
}
