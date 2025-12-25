
---

## 🧩 `http-deployment/README.md`

```markdown
# 🧩 Quarkus Multi-Tenancy HTTP Deployment

Handles build-time registration of HTTP resolvers and configuration.

## Responsibilities
- Auto-registers `TenantFilter`
- Wires all runtime resolvers (`HeaderTenantResolver`, etc.)
- Activates based on configuration flags

Used internally by Quarkus — no runtime overhead.