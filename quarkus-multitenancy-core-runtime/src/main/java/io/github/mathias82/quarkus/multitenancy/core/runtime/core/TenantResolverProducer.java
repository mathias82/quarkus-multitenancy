package io.github.mathias82.quarkus.multitenancy.core.runtime.core;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.MultiTenantConfig;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.TenantStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TenantResolverProducer {

    @Inject
    MultiTenantConfig config;

    @Inject
    Instance<TenantResolver> resolvers;

    @Produces
    @ApplicationScoped
    public CompositeTenantResolver compositeResolver() {

        List<TenantResolver> active = new ArrayList<>();

        for (TenantResolver resolver : resolvers) {
            TenantStrategy strategy =
                    resolver.getClass().getAnnotation(TenantStrategy.class);

            if (strategy != null && config.strategy().contains(strategy.value())) {
                active.add(resolver);
            }
        }

        return new CompositeTenantResolver(active);
    }
}
