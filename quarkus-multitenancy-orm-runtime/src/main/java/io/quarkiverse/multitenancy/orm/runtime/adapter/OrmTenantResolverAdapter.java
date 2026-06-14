package io.quarkiverse.multitenancy.orm.runtime.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;

/**
 * Bridges the shared {@link TenantContext} into Hibernate ORM's tenant
 * resolution SPI. Registered as a {@code @PersistenceUnitExtension}, it hands
 * Hibernate the tenant currently held in the request-scoped context, falling
 * back to {@link #BOOTSTRAP_TENANT} when no context is active (for example
 * during schema bootstrap, outside of a request).
 */
@PersistenceUnitExtension
@ApplicationScoped
public class OrmTenantResolverAdapter implements io.quarkus.hibernate.orm.runtime.tenant.TenantResolver {

    /**
     * Tenant id used when no request-scoped tenant is available, such as during
     * Hibernate's bootstrap and schema handling.
     */
    public static final String BOOTSTRAP_TENANT = "__bootstrap";

    @Inject
    Instance<TenantContext> tenantContext;

    @Override
    public String getDefaultTenantId() {
        return BOOTSTRAP_TENANT;
    }

    @Override
    public String resolveTenantId() {
        if (tenantContext == null || tenantContext.isUnsatisfied()) {
            return BOOTSTRAP_TENANT;
        }

        return tenantContext.get()
                .getTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not set before ORM access"));
    }

    @Override
    public boolean isRoot(String tenantId) {
        return BOOTSTRAP_TENANT.equals(tenantId);
    }
}
