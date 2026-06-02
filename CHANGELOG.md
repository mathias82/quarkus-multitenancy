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

### Notes

- The extension is currently marked as preview while the API stabilizes.
- JWT signature verification behavior is still under discussion and should be finalized before relying on JWT tenant resolution in security-sensitive environments.