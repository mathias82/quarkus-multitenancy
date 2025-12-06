# Quarkus Multi-Tenancy (Tenant Resolver)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mathias82/quarkus-multitenancy.svg)](https://central.sonatype.com/artifact/io.github.mathias82/quarkus-multitenancy)
[![Javadoc](https://javadoc.io/badge2/io.github.mathias82/quarkus-multitenancy-runtime/javadoc.svg)](https://javadoc.io/doc/io.github.mathias82/quarkus-multitenancy-runtime)
[![Build](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Status](https://img.shields.io/badge/status-experimental-orange)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)

A Quarkus extension that resolves the current tenant for each HTTP request 
using pluggable strategies (header, JWT, cookie, path) and exposes it via 
a request-scoped TenantContext. Ideal for REST microservices that need 
multi-tenant logic without configuring multiple datasources or OIDC realms.

---

## 📌 About This Project

**Quarkus Multi-Tenancy** is an extension designed to standardize and simplify tenant resolution for Quarkus services.

It offers a production-friendly foundation for multi-tenant architectures:

- Consistent tenant identification per request
- Pluggable resolvers (header now, JWT/cookie/path soon)
- Minimal boilerplate code
- Future integration with datasources, caches, identity providers
- Published on **Maven Central**

Next step: *Quarkiverse compatibility* ✔️

---

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
  <version>0.1.3</version>
</dependency>

2️⃣ Configure it

Add properties in application.properties:

quarkus.multi-tenant.enabled=true
quarkus.multi-tenant.strategy=header
quarkus.multi-tenant.header-name=X-Tenant-Id
quarkus.multi-tenant.default-tenant=public

# JWT claim for tenant
quarkus.multi-tenant.strategy=jwt
quarkus.multi-tenant.jwt-claim=tenantId

3️⃣ Inject the TenantContext

import io.github.mathias82.quarkus.multitenant.runtime.context.TenantContext;

@Inject
TenantContext tenantContext;

public void someMethod() {
    String tenant = tenantContext.getTenantId().orElse("unknown");
}

4️⃣ Example REST Endpoint

`@Path("/tenant")
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public String getTenant() {
        return tenantContext.getTenantId().orElse("NO TENANT FOUND");
    }
}`

## Build
mvn clean install

🧪 Test It
curl http://localhost:8080/tenant
# → public

curl -H "X-Tenant-Id: acme" http://localhost:8080/tenant
# → acme

⚙️ Configuration Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `quarkus.multi-tenant.enabled` | boolean | `false` | Enables multi-tenancy |
| `quarkus.multi-tenant.strategy` | string | `header` | Strategy used (`header`, later `jwt`, etc.) |
| `quarkus.multi-tenant.header-name` | string | `X-Tenant-Id` | Header name for resolving tenant |
| `quarkus.multi-tenant.default-tenant` | string | `public` | Tenant returned when none is provided |
| `quarkus.multi-tenant.jwt-claim-name` | string | `tenant_id` | Claim name when using JWT strategy |

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







