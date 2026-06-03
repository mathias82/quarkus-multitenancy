package io.quarkiverse.multitenancy.core.runtime.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;

/**
 * Runs every available {@link TenantResolver} in turn and surfaces the first
 * non-{@link TenantResolution.NotApplicable} outcome. A {@link TenantResolution.Rejected}
 * short-circuits the chain — callers are expected to treat that as an
 * unrecoverable failure for the current request and must not fall back to a
 * default tenant.
 */
@ApplicationScoped
public class CompositeTenantResolver {

    private static final Logger logger = Logger.getLogger(CompositeTenantResolver.class);

    @Inject
    Instance<TenantResolver> resolvers;

    public TenantResolution resolve(TenantResolutionContext context) {
        if (resolvers.isUnsatisfied()) {
            logger.debug("No TenantResolvers found");
            return TenantResolution.notApplicable();
        }

        for (TenantResolver resolver : resolvers) {
            TenantResolution result = resolver.resolve(context);
            if (result instanceof TenantResolution.Resolved resolved) {
                logger.debugf("Tenant resolved by %s = '%s'",
                        resolver.getClass().getSimpleName(), resolved.tenantId());
                return result;
            }
            if (result instanceof TenantResolution.Rejected rejected) {
                logger.debugf("Tenant resolution rejected by %s: %s",
                        resolver.getClass().getSimpleName(), rejected.reason());
                return result;
            }
        }

        logger.debug("No resolver produced a tenant");
        return TenantResolution.notApplicable();
    }
}
