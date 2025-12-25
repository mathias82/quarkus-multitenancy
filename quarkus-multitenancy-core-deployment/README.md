
---

## ⚙️ `core-deployment/README.md`

```markdown
# ⚙️ Quarkus Multi-Tenancy Core Deployment

Provides Quarkus build-time integration for the core runtime.

## Responsibilities

- Registers `TenantContext` and `CompositeTenantResolver` as CDI beans
- Enables other modules (HTTP, ORM) to use the core API