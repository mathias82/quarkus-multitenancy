# 🧩 Quarkus Multi-Tenancy Extension

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-core-runtime.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-core-runtime)
[![Build](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)
![Status](https://img.shields.io/badge/status-stable-brightgreen)

> A modular, decoupled multi-tenancy framework for Quarkus —  
> supporting HTTP, ORM, and future extensions.


Quarkus Multi-Tenancy provides a generic tenant resolution API and reusable building blocks that can be integrated with HTTP (headers, cookies, JWT), Hibernate ORM, and custom extensions.
It abstracts tenant identification logic away from any specific technology
and exposes a consistent TenantContext you can inject anywhere.

Why this exists
- No standard tenant resolution abstraction exists in Quarkus.
- This library defines a generic runtime API usable across REST, ORM, OIDC, or messaging.
- It can evolve into a Quarkiverse extension.

---

💡 Designed for REST microservices or backend modules
it can run independently of HTTP, allowing tenant resolution in database, cache, or messaging layers.


## 📌 About This Project

**Quarkus Multi-Tenancy** is an extension designed to standardize and simplify tenant resolution for Quarkus services, provides a decoupled multi-layer architecture.

- A core runtime module that defines TenantResolver, TenantContext, and composition logic.
- Independent HTTP, database, and deployment layers built on top of the core.

This makes the extension modular, lightweight, and framework-agnostic,
so you can plug tenant resolution into HTTP requests, JPA datasources, or background jobs seamlessly.

- Consistent tenant identification per request
- Pluggable resolvers (header now, JWT/cookie/path soon)
- Minimal boilerplate code
- Future integration with datasources, caches, identity providers
- Published on **Maven Central**

Next step: *Quarkiverse compatibility* ✔️

---

## 📚 Modules

| Module | Description | Docs |
|--------|--------------|------|
| 🧠 **Core Runtime** | Defines `TenantContext`, `TenantResolver` | [Read more →](quarkus-multitenancy-core-runtime/README.md) |
| ⚙️ **Core Deployment** | Build-time Quarkus integration for core | [Read more →](quarkus-multitenancy-core-deployment/README.md) |
| 🌐 **HTTP Runtime** | Resolves tenants from header/JWT/cookie | [Read more →](quarkus-multitenancy-http-runtime/README.md) |
| 🧩 **HTTP Deployment** | Registers HTTP resolvers | [Read more →](quarkus-multitenancy-http-deployment/README.md) |
| 🧱 **ORM Runtime** | Integrates Hibernate ORM multi-tenancy | [Read more →](quarkus-multitenancy-orm-runtime/README.md) |
| ⚙️ **ORM Deployment** | Quarkus feature registration for ORM | [Read more →](quarkus-multitenancy-orm-deployment/README.md) |
| 🧪 **Demo App** | PostgreSQL multi-tenant REST demo | [Read more →](quarkus-multitenancy-demo/README.md) |

---

## 🚀 Quick Start

```bash
mvn clean install
cd quarkus-multitenancy-demo
mvn quarkus:dev
