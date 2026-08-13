# 🧪 Quarkus Multi-Tenancy Demo

A runnable demo of the Quarkus Multitenancy extension with:

- HTTP tenant resolution from header, cookie, path, and the configured default tenant
- deterministic strategy precedence (`header,cookie,path`)
- a custom `TenantResolver` that only accepts configured tenant datasources
- tenant-id validation for externally supplied values
- Hibernate ORM DATABASE multitenancy with separate PostgreSQL databases for `tenant1` and `tenant2`

JWT is intentionally not enabled in the default strategy chain. The extension requires a verified `JsonWebToken` source (SmallRye JWT, OIDC, or an authenticated custom producer) before enabling the `jwt` strategy.

## Run the demo

```bash
docker compose down -v
docker compose up -d
mvn quarkus:dev
```

Databases:

- `tenant1` → `localhost:5433/tenant1` → `user1 / pass1`
- `tenant2` → `localhost:5434/tenant2` → `user2 / pass2`

## Tenant resolution scenarios

Header:

```bash
curl -H "X-Tenant: tenant1" http://localhost:8080/api/users/tenant
```

Cookie:

```bash
curl --cookie "tenant_cookie=tenant2" http://localhost:8080/api/users/tenant
```

Path:

```bash
curl http://localhost:8080/api/users/tenant/path/tenant2
```

Default tenant (`tenant1`):

```bash
curl http://localhost:8080/api/users/tenant
```

When multiple inputs are present, the configured chain applies in order. For example, the header wins over the cookie.

## ORM isolation

Create one user in each tenant:

```bash
curl -X POST -H "X-Tenant: tenant1" -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@tenant1.test"}' \
  http://localhost:8080/api/users

curl -X POST -H "X-Tenant: tenant2" -H "Content-Type: application/json" \
  -d '{"name":"Bob","email":"bob@tenant2.test"}' \
  http://localhost:8080/api/users
```

Read each database independently:

```bash
curl -H "X-Tenant: tenant1" http://localhost:8080/api/users
curl -H "X-Tenant: tenant2" http://localhost:8080/api/users
```

## Tests

`TenantResolutionScenariosTest` runs without PostgreSQL and covers header, cookie, path, default-tenant resolution, and precedence.

`UserResourceTest` exercises the PostgreSQL-backed ORM flow when both demo databases are available.

From the repository root:

```bash
mvn -B clean install -Dno-format
```

The Postman collection remains available under `postman/demo.postman_collection.json`.
