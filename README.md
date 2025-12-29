# 🧩 Quarkus Multi-Tenancy Extension

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-core-runtime.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-core-runtime)
[![Build](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)
![Status](https://img.shields.io/badge/status-stable-brightgreen)

> A modular, decoupled multi-tenancy framework for Quarkus 
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

# 🧠 Quarkus Multi-Tenancy Core Runtime

The **core foundation** of the Quarkus Multi-Tenancy extension.  
It defines the base APIs used to resolve and isolate tenants across layers 
from HTTP requests to ORM and background jobs.

---

## 🚀 What It Does

This module provides:
- The **`TenantContext`** – a request-scoped CDI bean storing the active tenant.
- The **`TenantResolver`** – an interface for resolving tenant IDs dynamically.
- The **`CompositeTenantResolver`** – allows multiple resolvers (header, JWT, cookie) to cooperate.

---

## 🧩 Multi-Tenant Isolation Achieved

Using this module (along with the HTTP and ORM runtimes),  
each incoming request is **isolated per tenant**, both logically and physically:

✅ Each request carries a tenant identifier (e.g., `X-Tenant: tenant1`).  
✅ The active tenant is injected into `TenantContext`.  
✅ The ORM runtime automatically routes persistence operations to the correct datasource.  
✅ This ensures **complete data isolation** — each tenant’s data lives in its own database.

For example:

| Request | Header | Database Used |
|----------|---------|---------------|
| `GET /api/users` | `X-Tenant: tenant1` | `tenant1` PostgreSQL DB |
| `GET /api/users` | `X-Tenant: tenant2` | `tenant2` PostgreSQL DB |

This means:  
→ Users from tenant1 will **never see or modify** data from tenant2.  
→ The system scales horizontally with full **per-tenant isolation**.  

---

## ⚙️ Required Dependencies

To enable full multi-tenant operation, your application should include:

```xml
<dependencies>
    <!-- HTTP Runtime (for header/jwt/cookie resolution) -->
    <dependency>
        <groupId>io.github.mathias82</groupId>
        <artifactId>quarkus-multitenancy-http-runtime</artifactId>
        <version>0.1.15</version>
    </dependency>

    <!-- ORM Runtime (for Hibernate datasource switching) -->
    <dependency>
        <groupId>io.github.mathias82</groupId>
        <artifactId>quarkus-multitenancy-orm-runtime</artifactId>
        <version>0.1.15</version>
    </dependency>
</dependencies>

These two modules together provide:
- Tenant resolution from HTTP requests
- Runtime context propagation
- ORM-level datasource routing per tenant

💡 Example Usage

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/tenant")
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public String getTenant() {
        return tenantContext.getTenantId().orElse("NO TENANT FOUND");
    }
}

When you send:
curl -H "X-Tenant: tenant1" http://localhost:8080/tenant

Output: tenant1

And Hibernate ORM automatically connects to the datasource configured for tenant1.

| Layer            | Module                              | Responsibility                       |
| ---------------- | ----------------------------------- | ------------------------------------ |
| **HTTP Runtime** | `quarkus-multitenancy-http-runtime` | Resolves tenant per HTTP request     |
| **ORM Runtime**  | `quarkus-multitenancy-orm-runtime`  | Connects ORM layer to tenant context |

Together, they achieve full tenant-based isolation in Quarkus.

## 🚀 Quick Start

```bash
mvn clean install
cd quarkus-multitenancy-demo
mvn quarkus:dev

In order to test it import the demo.postman_collection.json into Postman Curl

🧭 Architecture Overview

[HTTP Request]
     ↓
[HTTP TenantResolver] (header/JWT/cookie)
     ↓
[TenantContext] (request-scoped)
     ↓
[ORM Resolver] → selects datasource (tenant1, tenant2)

👉 See the quarkus-multitenancy-demo README.md
 for full setup (Docker, Postman, tenants).








