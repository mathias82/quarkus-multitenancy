package io.github.mathias82.quarkus.multitenancy.core.runtime.context;

import jakarta.enterprise.context.RequestScoped;
import java.util.Optional;

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
