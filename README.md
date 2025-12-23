# Quarkus Multi-Tenancy (Tenant Resolver)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10)
[![Javadoc](https://javadoc.io/badge2/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10/javadoc.svg)](https://javadoc.io/doc/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10)
[![Build](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Status](https://img.shields.io/badge/status-stable-brightgreen)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)

A fully decoupled Quarkus extension that resolves the current tenant for each HTTP request 
using pluggable strategies (header, JWT, cookie) and exposes it via 
a request-scoped TenantContext. Ideal for REST microservices that need multi-tenant logic without managing multiple datasources or OIDC realms.

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

⚡ Highlights

✅ Fully decoupled architecture, core & HTTP modules are independent
✅ TenantContext API for per-request or per-operation tenant tracking
✅ Multiple resolvers (header, JWT, cookie)
✅ Composite resolver that chains multiple strategies
✅ Strongly typed configuration via @ConfigMapping
✅ Zero external dependencies
✅ Works in JVM and Native mode

## ✨ Features

✔ **TenantContext API** for easy tenant access  
✔ **Pluggable resolver strategy** (header available, JWT/path/cookie upcoming)  
✔ **Request-scoped CDI TenantContext**  
✔ **Strongly typed configuration** using `@ConfigMapping`  
✔ **Works in JVM and Native mode**  
✔ **Zero external dependencies** besides Quarkus

---

## 🚀 Getting Started

### 1️⃣ Add the dependency

```xml
<dependency>
  <groupId>io.github.mathias82</groupId>
  <artifactId>quarkus-multitenancy-runtime</artifactId>
  <version>0.1.10</version>
</dependency>

### 2️⃣ Configure it

Add properties in application.properties:

# Enable HTTP multi-tenancy
quarkus.multi-tenant.http.enabled=true

# Choose one or more strategies
quarkus.multi-tenant.http.strategy=header,jwt,cookie

# Header strategy
quarkus.multi-tenant.http.header-name=X-Tenant-Id

# JWT strategy
quarkus.multi-tenant.http.jwt-claim-name=tenant

# Cookie strategy
quarkus.multi-tenant.http.cookie-name=tenant_cookie

# Default fallback tenant
quarkus.multi-tenant.http.default-tenant=public


### 3️⃣ Inject the TenantContext

import io.github.mathias82.quarkus.multitenant.runtime.context.TenantContext;

@Inject
TenantContext tenantContext;

public void someMethod() {
    String tenant = tenantContext.getTenantId().orElse("unknown");
}

### 4️⃣ Example REST Endpoint

`@Path("/tenant")
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public String getTenant() {
        return tenantContext.getTenantId().orElse("NO TENANT FOUND");
    }
}`

### 🧪 Test Examples

HEADER
curl -H "X-Tenant-Id: acme" http://localhost:8080/tenant
# → acme

JWT
TOKEN=$(echo '{"tenant":"demo"}' | base64)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/tenant
# → demo

Cookie
curl --cookie "tenant_cookie=blue" http://localhost:8080/tenant
# → blue

## Build
mvn clean install

🧪 Test It
curl http://localhost:8080/tenant
# → public

curl -H "X-Tenant-Id: acme" http://localhost:8080/tenant
# → acme

🧩 Decoupled Architecture in Action

This extension is built as independent layers:

Layer	            Module	                                Purpose
Core Runtime	    quarkus-multitenancy-core-runtime	      Defines the API (TenantContext, TenantResolver)
Core Deployment	  quarkus-multitenancy-core-deployment	  Build-time registration for Quarkus
HTTP Runtime	    quarkus-multitenancy-http-runtime	      Adds request filter, resolvers for header/JWT/cookie/path
HTTP Deployment	  quarkus-multitenancy-http-deployment	  Build-time registration for HTTP
(Optional) Database Runtime	(upcoming)	                  Schema-based or datasource-level tenant resolution

## 📦 Modules & Artifacts (v0.1.10)

The **Quarkus Multi-Tenancy** extension is built as a **set of fully decoupled modules**,  
allowing you to use only the layers you need.  
Each module is independent and can run in **HTTP services**, **database integrations**, or **background jobs**.

---

### 🧩 1️⃣ Core Runtime  
**Artifact:** `io.github.mathias82:quarkus-multitenancy-core-runtime`

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-core-runtime/0.1.10.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-core-runtime/0.1.10)
[![Javadoc](https://javadoc.io/badge2/io.github.mathias82/quarkus-multitenancy-core-runtime/0.1.10/javadoc.svg)](https://javadoc.io/doc/io.github.mathias82/quarkus-multitenancy-core-runtime/0.1.10)

**Purpose:**  
- Provides the **core runtime foundation** of the multitenancy system.  
- Defines the `TenantContext`, `TenantResolver`, and `CompositeTenantResolver` APIs.  
- Fully **framework-agnostic** — not tied to HTTP, REST, or Quarkus Web.  
- Can be reused in **JPA**, **cache**, or **messaging** layers.

> 💡 This is the **foundation layer** — everything else builds on top of it.

---

### ⚙️ 2️⃣ Core Deployment  
**Artifact:** `io.github.mathias82:quarkus-multitenancy-core-deployment`

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-core-deployment/0.1.10.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-core-deployment/0.1.10)
[![Javadoc](https://javadoc.io/badge2/io.github.mathias82/quarkus-multitenancy-core-deployment/0.1.10/javadoc.svg)](https://javadoc.io/doc/io.github.mathias82/quarkus-multitenancy-core-deployment/0.1.10)

**Purpose:**  
- Integrates the **core runtime** with the Quarkus build lifecycle.  
- Automatically registers beans like `TenantContext` and `CompositeTenantResolver`.  
- Required only when building your own **Quarkus extensions**.

> ⚙️ Mainly used by developers extending the framework — not typical application code.

---

### 🌐 3️⃣ HTTP Runtime  
**Artifact:** `io.github.mathias82:quarkus-multitenancy-http-runtime`

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10)
[![Javadoc](https://javadoc.io/badge2/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10/javadoc.svg)](https://javadoc.io/doc/io.github.mathias82/quarkus-multitenancy-http-runtime/0.1.10)

**Purpose:**  
- Provides the **HTTP integration layer** for tenant resolution.  
- Includes implementations for:
  - `HeaderTenantResolver`
  - `CookieTenantResolver`
  - `JwtTenantResolver`
  - `PathTenantResolver`
- Uses `TenantFilter` to resolve and set the tenant in the `TenantContext`.

> 🌍 Perfect for **REST APIs** or any HTTP-based microservice that needs tenant separation.

---

### 🧰 4️⃣ HTTP Deployment  
**Artifact:** `io.github.mathias82:quarkus-multitenancy-http-deployment`

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy-http-deployment/0.1.10.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy-http-deployment/0.1.10)
[![Javadoc](https://javadoc.io/badge2/io.github.mathias82/quarkus-multitenancy-http-deployment/0.1.10/javadoc.svg)](https://javadoc.io/doc/io.github.mathias82/quarkus-multitenancy-http-deployment/0.1.10)

**Purpose:**  
- Registers the HTTP resolvers automatically during Quarkus build time.  
- Used internally to wire up `TenantFilter` and runtime beans.  
- Activated at build phase only — no runtime overhead.

> 🧩 Required only for Quarkus extension developers or advanced build integrations.

---

### 🔮 (Upcoming) Database Runtime  
**Artifact:** *(planned module)*

**Purpose:**  
- Introduces **schema-based multitenancy** for JDBC and Hibernate.  
- Dynamically sets the database schema (e.g., `SET SCHEMA 'tenant_abc'`).  
- Based entirely on the `core-runtime`, independent of HTTP or REST.

> 🧱 Future module designed for **multi-tenant persistence** layers.

---

## 🧭 Module Overview

| Module | Description | Depends On | Typical Usage |
|---------|--------------|-------------|----------------|
| **core-runtime** | Core TenantContext & Resolver APIs | — | All environments |
| **core-deployment** | Quarkus build-time registration | core-runtime | Extension development |
| **http-runtime** | Tenant resolution via HTTP (header, JWT, cookie, path) | core-runtime | REST microservices |
| **http-deployment** | Registers HTTP resolvers at build time | http-runtime | Quarkus extensions |
| **database-runtime** *(planned)* | Schema resolver for JDBC/Hibernate | core-runtime | Database integrations |

---

## 💡 TL;DR

- 🧩 **Core Runtime** — the heart of the system, fully decoupled and framework-agnostic.  
- 🌐 **HTTP Modules** — extend the core to handle web-based tenant resolution.  
- ⚙️ **Deployment Modules** — handle Quarkus build-time bean registration.  
- 🪶 Everything is **decoupled and modular** — use only what your service actually needs.


⚙️ Configuration Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `quarkus.multi-tenant.enabled` | boolean | `false` | Enables multi-tenancy |
| `quarkus.multi-tenant.strategy` | string | `header` | Strategy used (`header`, `jwt`, etc.) |
| `quarkus.multi-tenant.header-name` | string | `X-Tenant-Id` | Header name for resolving tenant |
| `quarkus.multi-tenant.default-tenant` | string | `public` | Tenant returned when none is provided |

🧱 Architecture Overview

┌──────────────────────────────┐
│        Incoming Request       │
└───────────────┬──────────────┘
                │
        (JAX-RS Filter)
                ▼
┌──────────────────────────────┐
│ TenantResolver (Header/JWT)  │
└───────────────┬──────────────┘
                │
                ▼
┌──────────────────────────────┐
│  TenantContext (RequestScoped)│
└───────────────┬──────────────┘
                │
                ▼
┌──────────────────────────────┐
│   Inject Tenant Anywhere     │
└──────────────────────────────┘

🤝 Contributing

Contributions are welcome!

Fork this repository

Create a feature branch

Submit a pull request

Ensure tests pass

A full CONTRIBUTING guide will be added soon.

📦 Publishing

Planned future steps:
- Publishing to Maven Central
- Submitting to Quarkiverse Hub

⭐ Support the Project

If you find this useful, give the repo a star, it motivates continued development ❤️














