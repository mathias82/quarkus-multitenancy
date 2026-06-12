package io.quarkiverse.multitenancy.core.runtime.context;

import java.util.Optional;

/**
 * Represents the per-request tenant context.
 */
public interface TenantContext {

    /**
     * Returns the tenant bound to the current request, when one has been set.
     *
     * @return the current tenant id, or {@link Optional#empty()} when no tenant
     *         has been resolved yet
     */
    Optional<String> getTenantId();

    /**
     * Binds a tenant to the current request, replacing any previously set value.
     *
     * @param tenantId the tenant id to associate with the current request
     */
    void setTenantId(String tenantId);

    /**
     * Clears the tenant bound to the current request.
     */
    void clear();
}
