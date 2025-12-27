package io.github.mathias82.quarkus.multitenancy.orm.runtime.ctx;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import java.util.Optional;

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
