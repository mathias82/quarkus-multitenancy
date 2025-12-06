# Quarkus Multi-Tenancy

[![Build](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/mathias82/quarkus-multitenancy/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Status](https://img.shields.io/badge/status-experimental-orange)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)

Opinionated Quarkus extension that provides a simple, lightweight and configurable
**multi-tenant mechanism** for REST applications.

This extension lets you resolve the current tenant from an HTTP request (header strategy),
store it in a request-scoped CDI bean, and access it anywhere in your Quarkus application.

---

## About This Project

**Quarkus Multi-Tenancy** is an open-source extension designed to simplify tenant resolution
for Quarkus REST applications.  
It provides a lightweight, standardized way to identify the current tenant per incoming request using
strategies such as HTTP headers (default), JWT claims (upcoming), cookies (upcoming), or URL path segments (planned).

The goal of this project is to offer a reusable, production-ready multi-tenancy foundation that can
be used in both simple and complex microservice architectures.

This extension was created to:

- Provide a consistent multi-tenant mechanism across Quarkus services  
- Reduce boilerplate code for tenant extraction  
- Enable developers to plug custom tenant resolvers  
- Support future integration with datasources, caching layers and identity providers  
- Move toward Quarkiverse compatibility and Maven Central publishing  


## Features

- ✔ **TenantContext API** to access the current tenant ID from anywhere  
- ✔ **Pluggable resolver strategy** (header resolver included, JWT/path/cookie strategies coming soon)  
- ✔ **Lightweight JAX-RS request filter** that sets tenant per request  
- ✔ **Strongly typed configuration** via `@ConfigMapping`  
- ✔ **Zero dependencies other than Quarkus**  
- ✔ Works with JVM **and** Native Image  

---

## Getting Started

### 1. Add the dependency

```xml
<dependency>
  <groupId>io.github.mathias82</groupId>
  <artifactId>quarkus-multitenancy</artifactId>
  <version>0.1.0</version>
</dependency>

## Configure it:

Add properties in application.properties:

quarkus.multi-tenant.enabled=true
quarkus.multi-tenant.strategy=header
quarkus.multi-tenant.header-name=X-Tenant-Id
quarkus.multi-tenant.default-tenant=public

# JWT claim for tenant
quarkus.multi-tenant.strategy=jwt
quarkus.multi-tenant.jwt-claim=tenantId

## Inject the TenantContext:

import io.github.mathias82.quarkus.multitenant.runtime.context.TenantContext;

@Inject
TenantContext tenantContext;

public void someMethod() {
    String tenant = tenantContext.getTenantId().orElse("unknown");
}

## Example REST Endpoint

@Path("/tenant")
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public String getTenant() {
        return tenantContext.getTenantId().orElse("NO TENANT FOUND");
    }
}

## Build
mvn clean install

## Test It
curl http://localhost:8080/tenant
# → public

curl -H "X-Tenant-Id: acme" http://localhost:8080/tenant
# → acme

## Configuration Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `quarkus.multi-tenant.enabled` | boolean | `false` | Enables multi-tenancy |
| `quarkus.multi-tenant.strategy` | string | `header` | Strategy used (`header`, later `jwt`, etc.) |
| `quarkus.multi-tenant.header-name` | string | `X-Tenant-Id` | Header name for resolving tenant |
| `quarkus.multi-tenant.default-tenant` | string | `public` | Tenant returned when none is provided |
| `quarkus.multi-tenant.jwt-claim-name` | string | `tenant_id` | Claim name when using JWT strategy |

## Architecture Overview

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

## Publishing

Planned future steps:
- Publishing to Maven Central
- Submitting to Quarkiverse Hub






