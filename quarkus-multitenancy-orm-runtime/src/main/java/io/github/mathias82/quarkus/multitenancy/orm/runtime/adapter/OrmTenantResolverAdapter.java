package io.github.mathias82.quarkus.multitenancy.orm.runtime.adapter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@PersistenceUnitExtension
@ApplicationScoped
public class OrmTenantResolverAdapter implements io.quarkus.hibernate.orm.runtime.tenant.TenantResolver {

    @Inject
    TenantContext tenantContext;

    @Override
    public String getDefaultTenantId() {
        return "tenant1";
    }

    @Override
    public String resolveTenantId() {
        return tenantContext.getTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not set before ORM access"));
    }
}
