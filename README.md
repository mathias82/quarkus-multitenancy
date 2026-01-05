# 🧩 Quarkus Multi-Tenancy Extension
Modular Multi-Tenant Framework for Quarkus (HTTP, Hibernate ORM, SaaS)

Production-ready multi-tenancy for Quarkus applications. This repository provides a clean, decoupled and extensible solution for building multi-tenant SaaS platforms with Quarkus, Hibernate ORM and HTTP-based tenant resolution.

SEO keywords: quarkus multi tenancy, quarkus multitenant, quarkus saas, hibernate multi tenancy quarkus, tenant context quarkus.

---

## Why Quarkus Multi-Tenancy?

Quarkus does not provide a unified abstraction for tenant resolution across layers.
As a result, many teams duplicate tenant logic in REST, ORM and infrastructure code.

This project introduces:
- A single TenantContext available everywhere
- Explicit and testable tenant resolution
- Clean HTTP and Hibernate ORM integration
- Full data isolation per tenant

Perfect for SaaS, microservices and cloud-native architectures.

---

## Key Features

- Request-scoped TenantContext
- Pluggable TenantResolver API
- HTTP tenant resolution (headers, JWT, cookies)
- Hibernate ORM datasource routing
- Modular architecture (core, HTTP, ORM)
- Published on Maven Central
- Zero runtime overhead
- Production-ready design

---

## Architecture Overview

HTTP Request
→ HTTP TenantResolver
→ TenantContext (request-scoped)
→ ORM Resolver
→ Tenant-specific datasource

Each request is fully isolated per tenant.

---

## Multi-Tenant Isolation Example

GET /api/users with header X-Tenant: tenant1 → tenant1 database  
GET /api/users with header X-Tenant: tenant2 → tenant2 database  

Tenants never see each other’s data.

---

## Required Dependencies

Add both runtime modules to your Quarkus application:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.mathias82</groupId>
        <artifactId>quarkus-multitenancy-http-runtime</artifactId>
        <version>0.1.15</version>
    </dependency>

    <dependency>
        <groupId>io.github.mathias82</groupId>
        <artifactId>quarkus-multitenancy-orm-runtime</artifactId>
        <version>0.1.15</version>
    </dependency>
</dependencies>
```

---

## Example Usage

```java
@Path("/tenant")
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public String tenant() {
        return tenantContext.getTenantId().orElse("NO TENANT");
    }
}
```

```bash
curl -H "X-Tenant: tenant1" http://localhost:8080/tenant
```

Output:
tenant1

Hibernate ORM automatically routes to the correct datasource.

---

## Demo Application

A complete PostgreSQL multi-tenant demo is included:
- Docker Compose
- Multiple tenant databases
- REST APIs
- Postman collection

---

## Who Should Use This?

- SaaS developers using Quarkus
- Teams building multi-tenant REST APIs
- Architects designing clean tenant isolation
- Anyone using Hibernate ORM with multiple tenants

---

## Contributing

Issues, ideas and pull requests are welcome.
If this project helps you, please consider giving it a star.

---

This README is optimized for GitHub and Google search visibility.
