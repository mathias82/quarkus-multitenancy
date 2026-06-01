package io.quarkiverse.multitenancy.http.runtime.ctx;

import java.util.Optional;

import jakarta.ws.rs.container.ContainerRequestContext;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;

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
