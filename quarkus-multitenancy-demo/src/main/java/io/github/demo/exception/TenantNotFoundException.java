package io.github.demo.exception;

public class TenantNotFoundException extends RuntimeException {

    private final String tenantId;

    public TenantNotFoundException(String tenantId) {
        super("Tenant not found: " + tenantId);
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
