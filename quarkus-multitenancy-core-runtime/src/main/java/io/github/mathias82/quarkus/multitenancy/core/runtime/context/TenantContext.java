package io.github.mathias82.quarkus.multitenancy.core.runtime.context;

import java.util.Optional;

/**
 * Represents the per-request tenant context.
 */
public interface TenantContext {

    Optional<String> getTenantId();

    void setTenantId(String tenantId);

    void clear();
}

