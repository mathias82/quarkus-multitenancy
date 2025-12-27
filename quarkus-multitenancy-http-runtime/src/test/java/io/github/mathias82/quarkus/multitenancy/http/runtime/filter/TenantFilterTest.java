package io.github.mathias82.quarkus.multitenancy.http.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.Optional;

@Path("/tenant")
public class TenantFilterTest {

    @Inject
    TenantContext tenantContext;

    @GET
    public Optional<String> tenant() {
        return tenantContext.getTenantId();
    }
}
