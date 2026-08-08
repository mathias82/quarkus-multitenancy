# Quarkus Multitenancy Messaging Kafka Deployment

Build-time module for the optional Kafka tenant-propagation extension.

It registers the SmallRye Reactive Messaging interceptors, outgoing channel decorator, configuration mapping, and tenant validators used by `quarkus-multitenancy-messaging-kafka`.

Applications should depend on the runtime artifact rather than this deployment artifact directly.
