package io.github.mathias82.quarkus.multitenancy.http.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.CompositeTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import io.github.mathias82.quarkus.multitenancy.http.runtime.ctx.HttpTenantResolutionContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;

/**
 * HTTP request filter responsible for resolving the tenant
 * and populating the TenantContext for the current request.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TenantFilter implements ContainerRequestFilter {

    @Inject
    CompositeTenantResolver resolver;

    @Inject
    TenantContext tenantContext;

    @Inject
    HttpTenantConfig httpConfig;

    @PostConstruct
    void init() {
        System.out.println(">>> HttpTenantConfig.enabled = " + httpConfig.enabled());
        System.out.println(">>> HttpTenantConfig.headerName = " + httpConfig.headerName());
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (!httpConfig.enabled()) {
            return;
        }

        HttpTenantResolutionContext ctx =
                new HttpTenantResolutionContext(requestContext);

        Optional<String> tenant = resolver.resolve(ctx);

        String resolvedTenant =
                tenant.orElse(httpConfig.defaultTenant());

        tenantContext.setTenantId(resolvedTenant);
    }
}
