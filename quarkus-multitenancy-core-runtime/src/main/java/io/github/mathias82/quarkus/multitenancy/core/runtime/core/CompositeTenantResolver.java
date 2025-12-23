package io.github.mathias82.quarkus.multitenancy.core.runtime.core;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class CompositeTenantResolver {

    private static final Logger logger = Logger.getLogger(CompositeTenantResolver.class);

    @Inject
    Instance<TenantResolver> resolvers;

    public Optional<String> resolve(TenantResolutionContext context) {
        if (resolvers.isUnsatisfied()) {
            logger.debug("No TenantResolvers found");
            return Optional.empty();
        }

        for (TenantResolver resolver : resolvers) {
            Optional<String> result = resolver.resolve(context);
            if (result.isPresent()) {
                logger.debugf("Tenant resolved by %s = '%s'", resolver.getClass().getSimpleName(), result.get());
                return result;
            }
        }

        logger.debug("No resolver returned tenant");
        return Optional.empty();
    }
}
