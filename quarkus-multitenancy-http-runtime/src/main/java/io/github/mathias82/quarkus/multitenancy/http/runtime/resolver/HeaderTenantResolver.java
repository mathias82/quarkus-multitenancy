package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.TenantStrategy;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Optional;

@TenantStrategy("header")
@ApplicationScoped
public class HeaderTenantResolver implements TenantResolver {

    private final HttpTenantConfig config;

    @Inject
    public HeaderTenantResolver(HttpTenantConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> resolve(TenantResolutionContext context) {

        Optional<MultivaluedMap> raw =
                context.get(MultivaluedMap.class);

        if (raw.isEmpty()) {
            return Optional.empty();
        }

        MultivaluedMap<String, String> headers =
                (MultivaluedMap<String, String>) raw.get();

        String value = headers.getFirst(config.headerName());

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }
}
