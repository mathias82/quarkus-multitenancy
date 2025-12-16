package io.github.mathias82.quarkus.multitenancy.core.runtime.core;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;

import java.util.List;
import java.util.Optional;

public class CompositeTenantResolver {

    private final List<TenantResolver> resolvers;

    public CompositeTenantResolver(List<TenantResolver> resolvers) {
        this.resolvers = resolvers;
    }

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
