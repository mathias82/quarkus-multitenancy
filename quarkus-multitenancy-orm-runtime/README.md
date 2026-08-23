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
```

The default Hibernate ORM persistence unit is integrated automatically. Select
named multitenant persistence units explicitly:

```properties
quarkus.multi-tenant.orm.named-persistence-units=users,inventory

quarkus.hibernate-orm."users".multitenant=DATABASE
quarkus.hibernate-orm."inventory".multitenant=SCHEMA
```

Each selected persistence unit obtains its tenant identifier from the shared
request-scoped `TenantContext`. Unknown names and selected persistence units
without Hibernate ORM multitenancy fail at build time. Non-selected units do
not receive a named adapter.

## How It Works

`OrmTenantResolverAdapter` bridges the shared `TenantContext` into Quarkus'
Hibernate ORM tenant system. It uses the reserved `__bootstrap` tenant only
during ORM bootstrap; normal ORM access requires a tenant in `TenantContext`.

The adapter is a CDI default bean. An application can provide its own
`TenantResolver` with the matching `@PersistenceUnitExtension` qualifier to
override it for the default or a named persistence unit without ambiguity.
