package io.github.mathias82.quarkus.multitenancy.http.runtime.ctx;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.Optional;

public class HttpTenantResolutionContext implements TenantResolutionContext {

    private final ContainerRequestContext requestContext;

    public HttpTenantResolutionContext(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public <T> Optional<T> get(Class<T> type) {
        if (type.isInstance(requestContext)) {
            return Optional.of(type.cast(requestContext));
        }

        if (type.isAssignableFrom(requestContext.getHeaders().getClass())) {
            return Optional.of(type.cast(requestContext.getHeaders()));
        }

        return Optional.empty();
    }
}
