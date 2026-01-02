# 🧱 Quarkus Multi-Tenancy ORM Runtime

Connects the **core** tenant abstraction with Quarkus Hibernate ORM multi-tenancy.

## Configuration

```properties
quarkus.hibernate-orm.multitenant=DATABASE

quarkus.datasource.tenant1.jdbc.url=jdbc:postgresql://localhost:5433/tenant1
quarkus.datasource.tenant1.username=user1
quarkus.datasource.tenant1.password=pass1

quarkus.datasource.tenant2.jdbc.url=jdbc:postgresql://localhost:5434/tenant2
quarkus.datasource.tenant2.username=user2
quarkus.datasource.tenant2.password=pass2


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
