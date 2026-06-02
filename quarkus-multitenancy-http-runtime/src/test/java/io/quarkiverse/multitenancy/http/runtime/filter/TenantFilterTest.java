package io.quarkiverse.multitenancy.http.runtime.filter;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

@Path("/tenant")
public class TenantFilterTest {

    @Inject
    TenantContext tenantContext;

    @GET
    public Optional<String> tenant() {
        return tenantContext.getTenantId();
    }
}
