package io.github.mathias82.quarkus.multitenancy.http.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.CompositeTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.ctx.HttpTenantResolutionContext;
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
        System.out.println(">>> Incoming X-Tenant header: " + requestContext.getHeaderString("X-Tenant"));

        HttpTenantResolutionContext ctx = new HttpTenantResolutionContext(requestContext);
        Optional<String> tenant = resolver.resolve(ctx);

        System.out.println(">>> Resolver returned: " + tenant.orElse("<none>"));

        tenantContext.setTenantId(tenant.orElse("public"));
    }
}
