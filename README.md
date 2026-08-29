# 🧩 Quarkus Multitenancy

[![Build](https://github.com/quarkiverse/quarkus-multitenancy/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-multitenancy/actions/workflows/build.yml)
[![Documentation](https://img.shields.io/badge/docs-Quarkiverse-blue)](https://docs.quarkiverse.io/quarkus-multitenancy/dev/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-21%2B-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-red)
![Status](https://img.shields.io/badge/status-preview-orange)

> **Resolve a tenant once. Keep it across HTTP, reactive code, Kafka, background work, and Hibernate ORM.**

Quarkus Multitenancy is a modular Quarkiverse extension for applications that need one consistent tenant identity across application boundaries.

Instead of resolving and passing tenant identifiers independently in every layer, the extension provides a shared request-scoped `TenantContext` and integrations that resolve, propagate, validate, and consume that context where it matters.

```text
                         ┌──────────────────────────┐
 HTTP request            │ header · cookie · JWT   │
 ───────────────────────▶│ path · custom resolver │
                         └────────────┬─────────────┘
                                      │
                                      ▼
                             ┌─────────────────┐
                             │  TenantContext  │
                             └───────┬─────────┘
                                     │
                 ┌───────────────────┼────────────────────┐
                 │                   │                    │
                 ▼                   ▼                    ▼
        Reactive / worker      Hibernate ORM         Kafka producer
             work             tenant routing       X-Tenant header
                                                        │
                                                        ▼
                                                  Kafka consumer
                                                        │
                                                        ▼
                                                  TenantContext
```

## Why use it?

A multitenant application usually has more than one tenant boundary. A tenant may arrive in an HTTP header, a JWT claim, a cookie, or a URL path; the same request may then access Hibernate ORM, execute reactive work, and publish Kafka messages consumed by another service.

Without a shared contract, each integration tends to invent its own tenant lookup and propagation rules.

Quarkus Multitenancy gives those boundaries one model:

- **Resolve once** — built-in HTTP strategies plus custom CDI resolvers.
- **Fail safely** — distinguish missing input from explicitly rejected/untrusted input.
- **Validate external tenant IDs** — configurable validation and reserved-ID protection.
- **Keep context through Quarkus reactive boundaries** — `Uni`, `@Blocking`, and context-aware `ManagedExecutor` work remain tenant-aware within the active request.
- **Propagate through Kafka** — automatically stamp the tenant on outgoing records and restore it for consumers.
- **Route Hibernate ORM** — bridge the same `TenantContext` into multitenant persistence units.
- **Run tenant-scoped jobs** — explicitly bind a tenant for synchronous scheduled or background work.
- **Adopt incrementally** — HTTP, Kafka, and ORM integrations are separate modules.

> [!IMPORTANT]
> The extension handles **tenant identification and propagation**. Database/schema isolation, cache isolation, and authorization remain explicit application/framework concerns.

## 60-second example

### 1. Resolve the tenant at HTTP ingress

```properties
quarkus.multi-tenant.http.strategy=header,jwt,cookie,path
quarkus.multi-tenant.http.header-name=X-Tenant
quarkus.multi-tenant.http.jwt-claim-name=tenant
quarkus.multi-tenant.http.default-tenant=public
quarkus.multi-tenant.http.path-pattern=^/t/([^/]+)(?:/|$)
```

A request such as:

```http
POST /orders
X-Tenant: acme
```

establishes `acme` in the shared context:

```java
@Inject
TenantContext tenantContext;

String tenant = tenantContext.getTenantId().orElseThrow();
```

### 2. Use the same tenant in Hibernate ORM

Add `quarkus-multitenancy-orm` and configure Quarkus Hibernate ORM multitenancy. The extension bridges the active `TenantContext` into Hibernate ORM's tenant resolver.

Application code does not need to parse `X-Tenant` again before persistence access.

### 3. Send it through Kafka

Add `quarkus-multitenancy-messaging-kafka` and publish normally:

```java
@Inject
@Channel("orders")
Emitter<String> orders;

void publish(String order) {
    orders.send(order);
}
```

With tenant `acme` active, the Kafka record automatically carries:

```text
X-Tenant: acme
```

On the consumer side, enable the Quarkus Messaging request scope:

```properties
quarkus.messaging.request-scoped.enabled=true
```

Then consume normally:

```java
@Incoming("orders")
void consume(String order) {
    String tenant = tenantContext.getTenantId().orElseThrow();
    // tenant == "acme"
}
```

The incoming header is validated before it reaches `TenantContext`, and the previous context is restored across the asynchronous acknowledgment lifecycle.

**One tenant identity, from HTTP ingress to persistence to Kafka consumer.**

For the complete flow, explicit-header precedence, missing-tenant policies, validation, and failure handling, see the [Kafka tenant propagation guide](docs/modules/ROOT/pages/kafka-tenant-propagation.adoc).

## Install only what you need

### HTTP resolution

```xml
<dependency>
    <groupId>io.quarkiverse.multitenancy</groupId>
    <artifactId>quarkus-multitenancy-http</artifactId>
    <version>${quarkus-multitenancy.version}</version>
</dependency>
```

Built-in strategies:

```text
header → cookie → JWT claim → path → custom CDI resolvers
```

The configured strategy order is deterministic. Custom `TenantResolver` beans execute before built-ins and can use Jakarta `@Priority` when several resolvers may apply.

Resolver outcomes are explicit:

| Outcome | Meaning |
| --- | --- |
| `Resolved` | A trusted tenant was found |
| `NotApplicable` | No applicable input; resolution may continue |
| `Rejected` | Input was present but invalid/untrusted; do not silently fall back |

### Kafka propagation

```xml
<dependency>
    <groupId>io.quarkiverse.multitenancy</groupId>
    <artifactId>quarkus-multitenancy-messaging-kafka</artifactId>
    <version>${quarkus-multitenancy.version}</version>
</dependency>
```

The integration is scoped to SmallRye Reactive Messaging Kafka channels and supports:

- outgoing tenant-header injection
- incoming tenant restoration
- application-supplied header precedence
- strict missing-tenant policies
- UTF-8 and tenant-ID validation
- custom `KafkaTenantValidator` beans
- reserved tenant-ID protection
- async ack/nack lifecycle cleanup

### Hibernate ORM

```xml
<dependency>
    <groupId>io.quarkiverse.multitenancy</groupId>
    <artifactId>quarkus-multitenancy-orm</artifactId>
    <version>${quarkus-multitenancy.version}</version>
</dependency>
```

The ORM adapter consumes the same `TenantContext` used by the HTTP and Kafka integrations. The default persistence unit is supported automatically, and named multitenant persistence units can opt in explicitly.

```properties
quarkus.multi-tenant.orm.named-persistence-units=users,inventory
```

## Tenant context across boundaries

| Boundary | Tenant behavior |
| --- | --- |
| HTTP ingress | Resolve and validate through the configured resolver chain |
| `Uni`, `@Blocking`, `ManagedExecutor` inside the request | Quarkus preserves the active request context |
| Raw executor | Pass the tenant explicitly or use an appropriate context-aware mechanism |
| Synchronous background job | `TenantContextRunner.runAsTenant(...)` |
| Kafka producer | `TenantContext` → Kafka record header |
| Kafka consumer | Kafka record header → validated `TenantContext` |
| Hibernate ORM | `TenantContext` → ORM tenant resolver |

`TenantContext` is request scoped. It is intentionally not a global variable or an ordinary thread-local value.

## Tenant-scoped background work

For scheduled jobs, startup observers, maintenance callbacks, or other synchronous work outside HTTP ingress:

```java
@Inject
TenantContextRunner tenantRunner;

void refresh() {
    tenantRunner.runAsTenant("acme", () -> {
        // TenantContext contains "acme" here.
    });
}
```

The previous tenant is restored after completion or failure. `TenantContextRunner` is deliberately synchronous and rejects asynchronous results that could outlive the temporary binding.

## Security model

Tenant identifiers received from external boundaries are untrusted input.

HTTP and Kafka integrations support configurable maximum length and full-match regular-expression validation. Internal identifiers such as `__bootstrap` are reserved and cannot be selected from external tenant input.

Kafka consumers can also install domain-specific validators:

```java
@ApplicationScoped
public class RegisteredTenantValidator implements KafkaTenantValidator {

    @Override
    public Optional<String> validate(String tenantId) {
        return tenantExists(tenantId)
                ? Optional.empty()
                : Optional.of("tenant is not registered");
    }
}
```

For tenant-sensitive Kafka workloads, strict missing-tenant handling can make the boundary fail closed instead of processing messages without tenant metadata.

## Modules

| Module | Responsibility |
| --- | --- |
| `quarkus-multitenancy-core-runtime` | `TenantContext`, resolver contracts, validation foundations, `TenantContextRunner` |
| `quarkus-multitenancy-http-*` | HTTP resolution and validation |
| `quarkus-multitenancy-messaging-kafka-*` | Kafka propagation and validation |
| `quarkus-multitenancy-orm-*` | Hibernate ORM bridge |
| `quarkus-multitenancy-demo` | Runnable HTTP + PostgreSQL tenant-routing demo |

## Try the demo

```bash
git clone https://github.com/quarkiverse/quarkus-multitenancy.git
cd quarkus-multitenancy
mvn clean install
cd quarkus-multitenancy-demo
mvn quarkus:dev
```

The demo covers header, cookie, path, and default tenant resolution and includes PostgreSQL-backed tests that verify tenant database routing and isolation.

## Documentation

📚 **[Quarkiverse documentation](https://docs.quarkiverse.io/quarkus-multitenancy/dev/)**

Start with:

- [Tenant context propagation](docs/modules/ROOT/pages/context-propagation.adoc)
- [Kafka tenant propagation](docs/modules/ROOT/pages/kafka-tenant-propagation.adoc)
- [Runtime contracts and validation boundaries](docs/modules/ROOT/pages/runtime-contracts.adoc)
- [Programmatic tenant connections](docs/modules/ROOT/pages/programmatic-tenant-connections.adoc)
- [Migration notes for 0.2](docs/modules/ROOT/pages/migration-0.2.adoc)

## Contributing

Issues, bug reports, documentation improvements, and pull requests are welcome. Keep integrations modular, tenant boundaries explicit, and externally controlled tenant identifiers treated as untrusted input.

## License

Licensed under the [Apache License 2.0](LICENSE).
