# Release notes — `0.1.0` (preview)

First Quarkiverse preview release of the **Quarkus Multitenancy** extension.

> **Status:** preview. The API may evolve before `1.0`. Pin the exact version while we stabilise.

---

## What's in this release

A modular tenant-resolution layer for Quarkus, split across three extensions:

| Extension | Purpose |
|-----------|---------|
| `io.quarkiverse.multitenancy:quarkus-multitenancy-core` | `TenantResolver` SPI, request-scoped `TenantContext`, sealed `TenantResolution` outcome. |
| `io.quarkiverse.multitenancy:quarkus-multitenancy-http` | HTTP-side resolvers: header, JWT claim (verified bearer), cookie, path. |
| `io.quarkiverse.multitenancy:quarkus-multitenancy-orm` | Bridges `TenantContext` into Hibernate ORM multitenancy use cases. |

Add the modules you need; the core is pulled transitively.

```xml
<dependency>
  <groupId>io.quarkiverse.multitenancy</groupId>
  <artifactId>quarkus-multitenancy-http</artifactId>
  <version>0.1.0</version>
</dependency>
```

---

## Highlights

### Three-state tenant resolution (`Resolved` / `NotApplicable` / `Rejected`)

`TenantResolver` now returns a sealed `TenantResolution`. The dispatcher distinguishes between:

- `Resolved` — a resolver produced a tenant id.
- `NotApplicable` — the resolver had no input to process; the next strategy in the chain may try.
- `Rejected` — the resolver found a present-but-invalid input. The request is aborted with HTTP 401 instead of silently falling back to `defaultTenant`.

This closes the "silent downgrade" path where an unverifiable JWT used to be treated as an absent tenant.

### Verified JWT bearer tokens

The `jwt` strategy now requires a verified `JsonWebToken`. SmallRye JWT or Quarkus OIDC must be configured before the application boots.

Operator setup:

```properties
# SmallRye JWT
mp.jwt.verify.publickey.location=publicKey.pem
mp.jwt.verify.publickey.algorithm=RS256
mp.jwt.verify.issuer=https://your-issuer.example.com

# OR Quarkus OIDC (anonymous or named-tenant)
quarkus.oidc.auth-server-url=https://your-issuer.example.com
quarkus.oidc.customer.auth-server-url=https://customer.example.com
```

A startup check fails fast if `jwt` is enabled with no verification source. Applications that produce their own `JsonWebToken` outside of SmallRye/OIDC can opt out with `quarkus.multi-tenant.http.jwt.skip-startup-check=true`.

### Path-based resolution

A `PathTenantResolver` extracts the tenant from a configurable URL pattern (`^/t/([^/]+)(?:/|$)` by default). It is **not** in the default chain — opt in by adding `path` to `quarkus.multi-tenant.http.strategy`.

### Quarkiverse alignment

- Maven coordinates moved to `io.quarkiverse.multitenancy:*`.
- Parent POM `quarkiverse-parent:22`.
- Daily CI against the Quarkus snapshot via `quarkus-ecosystem-ci`.
- Asciidoc documentation published at <https://docs.quarkiverse.io/quarkus-multitenancy/dev/>.

---

## Upgrade notes

This is the first release; there is no upgrade path. If you used the pre-Quarkiverse `io.github.mathias82:*` snapshots, replace the coordinates with `io.quarkiverse.multitenancy:*` and migrate any custom `TenantResolver` implementations to the new `TenantResolution` return type:

```java
// Before
public Optional<String> resolve() { ... }

// After
public TenantResolution resolve() {
    return TenantResolution.resolved("acme");
    // or TenantResolution.notApplicable();
    // or TenantResolution.rejected("invalid input");
}
```

---

## Coming next

- Default strategy chain reconsidered (issue [#15](https://github.com/quarkiverse/quarkus-multitenancy/issues/15)) — `jwt` becomes opt-in via a deprecation cycle.
- Hardening of the resolved tenant identifier (issue [#16](https://github.com/quarkiverse/quarkus-multitenancy/issues/16)) — length cap, character whitelist, log-injection sanitisation enforced at the SPI boundary.

---

## Acknowledgements

Maintainers: [@mathias82](https://github.com/mathias82), [@lu1tr0n](https://github.com/lu1tr0n).

Thanks to everyone who reviewed and bumped dependencies during the Quarkiverse migration.
