package io.github.mathias82.quarkus.multitenancy.core.runtime.context;

import jakarta.enterprise.context.RequestScoped;

import java.util.Optional;

/**
 * Default runtime TenantContext implementation.
 *
 * Scoped per execution context (HTTP request by default),
 * but may also be used by other adapters (gRPC, messaging, batch).
 */
@RequestScoped
public class DefaultTenantContext implements TenantContext {

    private String tenantId;

    @Override
    public Optional<String> getTenantId() {
        if (tenantId == null) {
            return Optional.empty();
        }
        return Optional.of(tenantId);
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void clear() {
        this.tenantId = null;
    }
}
