package io.github.mathias82.quarkus.multitenancy.orm.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class OrmTenantHeaderFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(OrmTenantHeaderFilter.class);
    private static final String TENANT_HEADER = "X-Tenant";

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String tenant = requestContext.getHeaderString(TENANT_HEADER);

        if (tenant == null || tenant.isBlank()) {
            throw new WebApplicationException("Missing X-Tenant header", 400);
        }

        tenantContext.setTenantId(tenant);
        logger.infof("Tenant resolved from header: '%s'", tenant);
    }
}
