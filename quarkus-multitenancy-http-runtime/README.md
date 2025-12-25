# 🧱 Quarkus Multi-Tenancy ORM Runtime

Connects the **core** tenant abstraction with Quarkus Hibernate ORM multi-tenancy.

## How It Works
Bridges the `TenantResolver` API into Quarkus' ORM tenant system.

## Example Implementation

```java
@PersistenceUnitExtension
@ApplicationScoped
public class OrmTenantResolverAdapter implements io.quarkus.hibernate.orm.runtime.tenant.TenantResolver {

    @Inject TenantResolver coreResolver;
    @Inject TenantContext tenantContext;

    @Override
    public String getDefaultTenantId() {
        return "tenant1";
    }

    @Override
    public String resolveTenantId() {
        return coreResolver.resolve(new OrmTenantResolutionContext())
                           .or(() -> tenantContext.getTenantId())
                           .orElse(getDefaultTenantId());
    }
}
