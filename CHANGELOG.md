# Changelog

## 0.1.0 - Unreleased

Initial Quarkiverse preview release.

### Added

- Core tenant resolution API.
- Request-scoped tenant context.
- HTTP tenant resolution strategies:
    - header
    - JWT claim
    - cookie
    - path
- ORM integration module.
- Quarkus extension descriptors.
- Quarkiverse CI/CD release workflow setup.
- Initial documentation module.

### Changed

- `TenantResolver.resolve` now returns a sealed `TenantResolution` (`Resolved` / `NotApplicable` / `Rejected`) instead of `Optional<String>`. A present-but-invalid input rejects the request with HTTP 401 rather than silently falling back to the default tenant.
- The JWT tenant strategy now requires SmallRye JWT (or OIDC) for token verification. The previous base64-decode path that read claims from unsigned tokens has been removed; the resolver injects the verified `JsonWebToken` and rejects requests whose token cannot be verified or is missing the configured claim.

### Notes

- The extension is currently marked as preview while the API stabilizes.
- Operators enabling the `jwt` strategy must configure `mp.jwt.verify.*` or `quarkus.oidc.*` so SmallRye JWT can verify incoming tokens. Set `quarkus.multi-tenant.http.jwt.skip-startup-check=true` to opt out of the startup check when a custom `JsonWebToken` producer is in use.