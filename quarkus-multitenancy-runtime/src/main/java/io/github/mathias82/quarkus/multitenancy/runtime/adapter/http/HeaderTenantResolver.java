package io.github.mathias82.quarkus.multitenancy.runtime.adapter.http;

import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.runtime.config.MultiTenantConfig;
import io.github.mathias82.quarkus.multitenancy.runtime.config.TenantStrategy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Optional;

@ApplicationScoped
@TenantStrategy("header")
@Priority(100)
public class HeaderTenantResolver implements TenantResolver {

    private final MultiTenantConfig config;

    @Inject
    public HeaderTenantResolver(MultiTenantConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> resolve(TenantResolutionContext context) {

        Optional<Object> raw = context.get(MultivaluedMap.class);
        if (raw.isEmpty()) {
            return Optional.empty();
        }

        MultivaluedMap<String, String> headers =
                (MultivaluedMap<String, String>) raw.get();

        String value = headers.getFirst(config.headerName());

        if (value != null && !value.isBlank()) {
            return Optional.of(value);
        }

        String def = config.defaultTenant();
        if (def != null && !def.isBlank()) {
            return Optional.of(def);
        }

        return Optional.empty();
    }
}
