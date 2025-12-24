package io.github.mathias82.quarkus.multitenancy.orm.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.CompositeTenantResolver;
import io.github.mathias82.quarkus.multitenancy.orm.runtime.ctx.OrmTenantResolutionContext;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class OrmTenantHeaderFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(OrmTenantHeaderFilter.class);

    @Inject
    CompositeTenantResolver resolver;

    @Inject
    TenantContext tenantContext;

    private static final String DEFAULT_TENANT = "tenant1";

    @Override
    public void filter(ContainerRequestContext requestContext) {

        logger.debugf("Incoming request: %s %s", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());

        OrmTenantResolutionContext ctx = new OrmTenantResolutionContext();
        Optional<String> tenant = resolver.resolve(ctx);

        String resolvedTenant = tenant.orElse(DEFAULT_TENANT);

        tenantContext.setTenantId(resolvedTenant);

        if (tenant.isPresent()) {
            logger.infof("Tenant successfully resolved: '%s'", resolvedTenant);
        } else {
            logger.warnf("No tenant resolved, falling back to default: '%s'", DEFAULT_TENANT);
        }
    }
}
