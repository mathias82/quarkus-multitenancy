# 🧩 Quarkus Multi-Tenancy Extension

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-core-runtime.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-core-runtime)
[![Website](https://img.shields.io/badge/Website-GitHub%20Pages-black)](https://mathias82.github.io/quarkus-multitenancy/)
[![Build](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)
![Status](https://img.shields.io/badge/status-stable-brightgreen)

> A production-ready, fully decoupled multi-tenancy extension for Quarkus.  
> Designed for SaaS, microservices and cloud-native architectures.

---

## ✨ Overview

**Quarkus Multi-Tenancy** provides a generic tenant resolution API and reusable building blocks that work consistently across:

- 🌐 HTTP (headers, cookies, JWT)
- 🧱 Hibernate ORM
- ⚙️ Background jobs & async processing

Tenant identification is abstracted away from infrastructure concerns and exposed through a **request-scoped `TenantContext`** that can be injected anywhere.

---

## 🤔 Why This Exists

Quarkus does not provide a unified abstraction for tenant resolution across layers.

This extension:
- avoids duplicating tenant-resolution logic per technology
- keeps tenant handling explicit and testable
- enables clean multi-tenant SaaS architectures
- works without framework lock-in

💡 It can run **with or without HTTP**, allowing tenant resolution in database, cache, or messaging layers.

⭐ If you find this useful, please consider giving it a star — it really helps visibility.

---

## 🧱 Architecture & Design

The project follows a **modular, layered architecture**:

- **Core Runtime** – tenant context & resolver APIs
- **HTTP Layer** – resolves tenant from requests
- **ORM Layer** – routes Hibernate datasources per tenant
- **Deployment Modules** – Quarkus build-time integration

This keeps the system lightweight, extensible and framework-agnostic.

---

## 📦 Modules

| Module | Description |
|------|------------|
| 🧠 **Core Runtime** | `TenantContext`, `TenantResolver`, composition logic |
| ⚙️ **Core Deployment** | Build-time Quarkus integration |
| 🌐 **HTTP Runtime** | Tenant resolution from header/JWT/cookie |
| 🧩 **HTTP Deployment** | Registers HTTP resolvers |
| 🧱 **ORM Runtime** | Hibernate ORM multi-tenancy integration |
| ⚙️ **ORM Deployment** | ORM feature registration |
| 🧪 **Demo App** | PostgreSQL multi-tenant REST demo |

---

## 🔐 Multi-Tenant Isolation

Using the HTTP + ORM runtimes together, each request is **fully isolated per tenant**:

| Request | Header | Database Used |
|-------|--------|---------------|
| `GET /api/users` | `X-Tenant: tenant1` | `tenant1` PostgreSQL |
| `GET /api/users` | `X-Tenant: tenant2` | `tenant2` PostgreSQL |

✔ Tenants never see or modify each other’s data  
✔ Horizontal scalability with full isolation  

---

## ⚙️ Required Dependencies

Add **both runtime modules** to enable end-to-end HTTP → ORM multi-tenancy:

```xml
<dependencies>
    <!-- HTTP runtime: tenant resolution via headers, JWT or cookies -->
    <dependency>
        <groupId>io.github.mathias82</groupId>
        <artifactId>quarkus-multitenancy-http-runtime</artifactId>
        <version>0.1.15</version>
    </dependency>

    <!-- ORM runtime: tenant-aware datasource routing (Hibernate ORM) -->
    <dependency>
        <groupId>io.github.mathias82</groupId>
        <artifactId>quarkus-multitenancy-orm-runtime</artifactId>
        <version>0.1.15</version>
    </dependency>
</dependencies> ``

These two modules together provide:
- Tenant resolution from HTTP requests
- Runtime context propagation
- ORM-level datasource routing per tenant

---

## 💡 Example Usage

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

---

## 🚀 Quick Start

```bash
mvn clean install
cd quarkus-multitenancy-demo
mvn quarkus:dev

In order to test it import the demo.postman_collection.json into Postman Curl

---

## 🧭 Architecture Overview

[HTTP Request]
     ↓
[HTTP TenantResolver] (header/JWT/cookie)
     ↓
[TenantContext] (request-scoped)
     ↓
[ORM Resolver] → selects datasource (tenant1, tenant2)

👉 See the quarkus-multitenancy-demo README.md
 for full setup (Docker, Postman, tenants).


⭐ If this extension helps you build cleaner multi-tenant systems, consider starring the repository.



















