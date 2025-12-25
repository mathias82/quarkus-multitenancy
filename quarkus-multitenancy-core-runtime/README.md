
---

## 🧠 `core-runtime/README.md`

```markdown
# 🧠 Quarkus Multi-Tenancy Core Runtime

Defines the **TenantContext** and **TenantResolver** APIs —  
the foundation for all multi-tenant logic.

## Features

- Abstract `TenantResolver` API
- Request-scoped `TenantContext`
- `CompositeTenantResolver` for chaining
- Framework-agnostic design (not tied to Quarkus HTTP)

## Example Usage

```java
@Inject
TenantContext tenantContext;

String tenant = tenantContext.getTenantId().orElse("public");