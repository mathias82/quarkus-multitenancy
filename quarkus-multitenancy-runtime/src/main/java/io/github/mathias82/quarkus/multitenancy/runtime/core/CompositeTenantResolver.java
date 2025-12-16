package io.github.mathias82.quarkus.multitenancy.runtime.core;

import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CompositeTenantResolver {

    @Inject
    List<TenantResolver> resolvers;

    public Optional<String> resolve(TenantResolutionContext context) {
        for (TenantResolver resolver : resolvers) {
            Optional<String> tenantId = resolver.resolve(context);
            if (tenantId.isPresent()) {
                return tenantId;
            }
        }
        return Optional.empty();
    }
}
