package io.quarkiverse.multitenancy.http.runtime.filter;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

/**
 * Test resource exposing endpoints under {@code /t/{tenant}/...} so the
 * {@code PathTenantResolver} has a request path that matches its default
 * pattern.
 */
@Path("/t/{tenant}/tenant")
public class PathTenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public Optional<String> tenant() {
        return tenantContext.getTenantId();
    }
}
