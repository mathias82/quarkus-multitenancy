# Quarkus Multitenancy Messaging Kafka Runtime

This optional extension module propagates `TenantContext` through Kafka record headers.

It stamps the current tenant on outgoing Kafka messages, restores the tenant for incoming Kafka handlers, validates untrusted incoming header values, and cleans up with the message acknowledgment lifecycle. Other Reactive Messaging connectors are left unchanged.

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

Incoming Kafka tenant identifiers are an external trust boundary and are validated before being bound to `TenantContext`. Outgoing propagation uses the tenant already held by the local application context and does not apply the incoming length/pattern policy to it.

By default, an incoming message with no tenant header is processed with no tenant bound. Applications that require fail-closed tenant handling should enable:

```properties
quarkus.multi-tenant.messaging.kafka.fail-on-missing-incoming-tenant=true
```

An invalid incoming tenant is rejected through message nack. Whether the Kafka channel continues after that nack is controlled by the SmallRye Kafka failure strategy. The connector default is fail-stop, so applications that must keep consuming after malformed tenant metadata should configure an appropriate strategy, for example `ignore` or a dead-letter queue, according to their delivery requirements.

See the project documentation for the complete strict missing-tenant and tenant-id validation options.
