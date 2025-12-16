package io.github.mathias82.quarkus.multitenancy.runtime.api;

import java.util.Optional;

public interface TenantResolutionContext {
    Optional<Object> get(Class<?> type);
}

