package io.github.mathias82.quarkus.multitenancy.orm.runtime.adapter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@PersistenceUnitExtension
@ApplicationScoped
public class OrmTenantResolverAdapter implements io.quarkus.hibernate.orm.runtime.tenant.TenantResolver {

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
                .orElseThrow(() ->
                        new IllegalStateException("Tenant not set before ORM access"));
    }

    @Override
    public boolean isRoot(String tenantId) {
        return BOOTSTRAP_TENANT.equals(tenantId);
    }
}


