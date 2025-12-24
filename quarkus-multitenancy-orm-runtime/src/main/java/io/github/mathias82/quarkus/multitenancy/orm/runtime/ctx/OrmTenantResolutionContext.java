package io.github.mathias82.quarkus.multitenancy.orm.runtime.ctx;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import java.util.Optional;

/**
 * ORM-specific TenantResolutionContext that may later include ORM-related metadata.
 */
public class OrmTenantResolutionContext implements TenantResolutionContext {

    @Override
    public <T> Optional<T> get(Class<T> type) {
        return Optional.empty();
    }
}
