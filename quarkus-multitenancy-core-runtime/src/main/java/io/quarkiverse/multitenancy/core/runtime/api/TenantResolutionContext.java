package io.quarkiverse.multitenancy.core.runtime.api;

import java.util.Optional;

public interface TenantResolutionContext {
    <T> Optional<T> get(Class<T> type);
}