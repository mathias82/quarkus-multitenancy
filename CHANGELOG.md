# Changelog

## 0.1.0 - Unreleased

Initial Quarkiverse preview release.

### Added

- Core tenant resolution API and CDI context.
- Request-scoped `TenantContext` for resolved tenant propagation.
- HTTP tenant resolution strategies:
    - header
    - JWT claim (verified bearer)
    - cookie
    - path
- ORM integration module bridging `TenantContext` into Hibernate ORM multitenancy use cases.
- `@ConfigMapping`-based configuration on `HttpTenantConfig` with discoverable, documented keys.
- Quarkus extension descriptors (`quarkus-extension.yaml`, build-time processors, codestart metadata).
- Quarkiverse CI/CD canonical workflow set (`build`, `pre-release`, `release`, `perform-release`).
- Daily CI against the Quarkus snapshot via `quarkus-ecosystem-ci`.
- Initial Asciidoc documentation module published at `docs.quarkiverse.io/quarkus-multitenancy`.

### Changed

- `TenantResolver.resolve` now returns a sealed `TenantResolution` (`Resolved` / `NotApplicable` / `Rejected`) instead of `Optional<String>`. A present-but-invalid input rejects the request with HTTP 401 rather than silently falling back to the default tenant.
- The JWT tenant strategy now requires SmallRye JWT (or Quarkus OIDC) for token verification. The previous base64-decode path that read claims from unsigned tokens has been removed; the resolver injects the verified `JsonWebToken` and rejects requests whose token cannot be verified or is missing the configured claim.
- README rewritten for the post-migration Quarkiverse coordinates and the verified JWT behaviour.
- Quarkiverse parent POM bumped to version 22.

### Operator notes

- The extension is marked as **preview** while the API stabilises. Source and binary compatibility may change before `1.0`.
- The default strategy chain is `header,jwt,cookie`. Applications that include the `jwt` strategy (explicitly or via the implicit default) **must** configure one of the following before the application boots:
    - SmallRye JWT verification via `mp.jwt.verify.publickey.*` and `mp.jwt.verify.issuer`.
    - Quarkus OIDC via `quarkus.oidc.auth-server-url` (or a named-tenant equivalent such as `quarkus.oidc.<tenant>.auth-server-url`).
- Applications that produce a custom `JsonWebToken` outside of SmallRye JWT / OIDC can opt out of the boot-time verification check with `quarkus.multi-tenant.http.jwt.skip-startup-check=true`.
- Tenants resolved from any strategy are propagated through the same `TenantContext`. The extension does not enforce length, charset, or log-injection guards on the resolved identifier; downstream consumers should treat the value as untrusted input until a future hardening release lands.
