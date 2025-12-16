package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.TenantStrategy;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.PathSegment;

import java.util.List;
import java.util.Optional;

@TenantStrategy("path")
@ApplicationScoped
@Priority(300)
public class PathTenantResolver implements TenantResolver {

    private final HttpTenantConfig config;

    @Inject
    public PathTenantResolver(HttpTenantConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {

        Optional<ContainerRequestContext> reqOpt =
                context.get(ContainerRequestContext.class);

        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }

        List<PathSegment> segments =
                reqOpt.get().getUriInfo().getPathSegments();

        int idx = config.pathSegmentIndex();
        if (idx < 0 || idx >= segments.size()) {
            return Optional.empty();
        }

        String tenant = segments.get(idx).getPath();
        if (tenant == null || tenant.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(tenant);
    }
}
