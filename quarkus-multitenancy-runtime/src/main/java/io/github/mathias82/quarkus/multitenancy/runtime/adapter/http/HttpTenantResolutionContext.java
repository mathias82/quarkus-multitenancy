package io.github.mathias82.quarkus.multitenancy.runtime.adapter.http;

import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolutionContext;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Optional;

public class HttpTenantResolutionContext implements TenantResolutionContext {

    private final MultivaluedMap<String, String> headers;

    public HttpTenantResolutionContext(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Optional<Object> get(Class<?> type) {
        if (type == MultivaluedMap.class) {
            return Optional.of(headers);
        }
        return Optional.empty();
    }
}
