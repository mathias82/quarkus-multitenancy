# Quarkus Multitenancy Messaging Kafka Runtime

This optional extension module propagates `TenantContext` through Kafka record headers.

It stamps the current tenant on outgoing messages, restores the tenant for incoming handlers, validates untrusted header values, and cleans up with the message acknowledgment lifecycle.

## Usage

```xml
<dependency>
    <groupId>io.quarkiverse.multitenancy</groupId>
    <artifactId>quarkus-multitenancy-messaging-kafka</artifactId>
    <version>${quarkus-multitenancy.version}</version>
</dependency>
```

Enable the Quarkus Messaging request scope and optionally customize the Kafka header:

```properties
quarkus.messaging.request-scoped.enabled=true
quarkus.multi-tenant.messaging.kafka.header-name=X-Tenant
```

See the project documentation for strict missing-tenant behavior and tenant-id validation options.
