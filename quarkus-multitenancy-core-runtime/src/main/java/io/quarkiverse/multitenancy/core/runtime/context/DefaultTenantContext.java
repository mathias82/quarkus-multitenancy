package io.quarkiverse.multitenancy.core.runtime.context;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class DefaultTenantContext implements TenantContext {

    private String tenantId;

    @Override
    public Optional<String> getTenantId() {
        return Optional.ofNullable(tenantId);
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void clear() {
        tenantId = null;
    }
}
