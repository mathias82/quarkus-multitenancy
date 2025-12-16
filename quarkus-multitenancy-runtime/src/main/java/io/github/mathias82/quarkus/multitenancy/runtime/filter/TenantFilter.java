package io.github.mathias82.quarkus.multitenancy.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.test.HttpTenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.runtime.context.TenantContext;
import io.github.mathias82.quarkus.multitenancy.runtime.resolution.CompositeTenantResolver;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class TenantFilter implements ContainerRequestFilter {

    @Inject
    CompositeTenantResolver resolver;

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        HttpTenantResolutionContext ctx =
                new HttpTenantResolutionContext(requestContext.getHeaders());

        Optional<String> tenant = resolver.resolve(ctx);
        if (tenant.isPresent()) {
            tenantContext.setTenantId(tenant.get());
        }
    }
}
