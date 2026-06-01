package io.quarkiverse.multitenancy.orm.runtime.ctx;

import java.util.Optional;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;

/**
 * ORM-specific TenantResolutionContext that may later include ORM-related metadata.
 */
public class OrmTenantResolutionContext implements TenantResolutionContext {

    private final String tenantId;

    public OrmTenantResolutionContext(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public <T> Optional<T> get(Class<T> type) {
        if (type.equals(String.class)) {
            return Optional.of(type.cast(tenantId));
        }
        return Optional.empty();
    }
}
