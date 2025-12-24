package io.github.mathias82.quarkus.multitenancy.orm.runtime.adapter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.CompositeTenantResolver;
import io.github.mathias82.quarkus.multitenancy.orm.runtime.ctx.OrmTenantResolutionContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Adapts the core multitenancy system to Quarkus Hibernate ORM.
 */
@PersistenceUnitExtension
@ApplicationScoped
public class OrmTenantResolverAdapter implements io.quarkus.hibernate.orm.runtime.tenant.TenantResolver {

    @Inject
    CompositeTenantResolver compositeResolver;

    @Inject
    TenantContext tenantContext;

    @Override
    public String getDefaultTenantId() {
        return "tenant1";
    }

    @Override
    public String resolveTenantId() {
        return compositeResolver.resolve(new OrmTenantResolutionContext())
                .or(() -> tenantContext.getTenantId())
                .orElse(getDefaultTenantId());
    }
}
