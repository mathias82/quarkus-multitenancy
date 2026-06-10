package io.quarkiverse.multitenancy.orm.runtime.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

/**
 * Shared JAX-RS resource for {@link OrmTenantHeaderFilter} integration tests.
 *
 * <p>
 * Echoes the value currently held by the request-scoped {@link TenantContext},
 * which lets the filter integration tests observe whether the filter populated
 * the context for the current request.
 */
@Path("/echo")
public class EchoResource {

    @Inject
    TenantContext tenantContext;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String echo() {
        return "tenant=" + tenantContext.getTenantId().orElse("(none)");
    }
}
