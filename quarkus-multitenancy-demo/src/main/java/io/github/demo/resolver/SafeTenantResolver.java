package io.github.demo.resolver;

import io.github.demo.exception.TenantNotFoundException;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.Optional;

@ApplicationScoped
public class SafeTenantResolver implements TenantResolver {

    private static final String HEADER_NAME = "X-Tenant";

    @Inject
    TenantDataSourceRegistry registry;

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);

        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }

        ContainerRequestContext req = reqOpt.get();
        String tenantId = req.getHeaderString(HEADER_NAME);

        if (tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }

        tenantId = tenantId.trim();

        if (!registry.exists(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }

        return Optional.of(tenantId);
    }
}
