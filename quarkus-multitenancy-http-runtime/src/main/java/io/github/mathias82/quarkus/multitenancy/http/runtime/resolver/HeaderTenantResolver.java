package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.TenantStrategy;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

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
    public Optional<String> resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }

        ContainerRequestContext req = reqOpt.get();
        String header = req.getHeaderString(config.headerName());
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(header.trim());
    }
}
