# Quarkus Multitenancy Messaging Kafka Runtime

This optional extension module propagates `TenantContext` through Kafka record headers.

It stamps the current tenant on outgoing Kafka messages, restores the tenant for incoming Kafka handlers, validates untrusted header values, and cleans up with the message acknowledgment lifecycle. Other Reactive Messaging connectors are left unchanged.

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

Rejected tenant metadata nacks only the affected message; it does not terminate the channel. See the project documentation for strict missing-tenant behavior and tenant-id validation options.
