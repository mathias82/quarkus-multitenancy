# 🌐 Quarkus Multi-Tenancy HTTP Runtime

Provides pluggable tenant resolvers for HTTP requests.

## Available Strategies

- `HeaderTenantResolver` — reads from `X-Tenant` header
- `CookieTenantResolver` — extracts from cookies
- `JwtTenantResolver` — extracts claim from JWT
- `PathTenantResolver` — resolves from URL path

## Configuration

```properties
quarkus.multi-tenant.http.enabled=true
quarkus.multi-tenant.http.strategy=header,jwt
quarkus.multi-tenant.http.header-name=X-Tenant
quarkus.multi-tenant.http.default-tenant=public


CDI Integration

@Inject
TenantContext tenantContext;

@GET
public String tenant() {
    return tenantContext.getTenantId().orElse("public");
}
