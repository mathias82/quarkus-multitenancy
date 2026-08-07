/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.multitenancy.messaging.kafka.deployment;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;

class KafkaTenantMessagingLifecycleTest {

    private static final String HEADER = "tenant-key";
    private static final String INCOMING_CHANNEL = "tenant-in";
    private static final String OUTGOING_CHANNEL = "tenant-out";

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar.addClasses(MessagingProbe.class, SenderResource.class))
            .overrideConfigKey("quarkus.kafka.devservices.enabled", "false")
            .overrideConfigKey("quarkus.messaging.request-scoped.enabled", "true")
            .overrideConfigKey("quarkus.multi-tenant.messaging.kafka.header-name", HEADER)
            .overrideConfigKey("mp.messaging.incoming." + INCOMING_CHANNEL + ".connector", "smallrye-in-memory")
            .overrideConfigKey("mp.messaging.incoming." + INCOMING_CHANNEL + ".run-on-vertx-context", "true")
            .overrideConfigKey("mp.messaging.outgoing." + OUTGOING_CHANNEL + ".connector", "smallrye-in-memory");

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector connector;

    @Inject
    MessagingProbe probe;

    @BeforeEach
    void resetProbe() {
        probe.reset();
    }

    @Test
    void shouldStampTenantThroughTheOutgoingMessagingPipeline() throws Exception {
        given()
                .queryParam("tenant", "acme-out")
                .when().get("/send")
                .then().statusCode(200);

        InMemorySink<String> sink = connector.sink(OUTGOING_CHANNEL);
        Message<String> sent = sink.received().getFirst();
        OutgoingKafkaRecordMetadata<?> metadata = sent.getMetadata(OutgoingKafkaRecordMetadata.class).orElseThrow();
        assertEquals("acme-out", new String(metadata.getHeaders().lastHeader(HEADER).value(), UTF_8));
    }

    @Test
    void shouldKeepIncomingTenantUntilAsyncAcknowledgmentAndNotLeakIt() throws Exception {
        InMemorySource<Message<String>> source = connector.source(INCOMING_CHANNEL);
        CompletableFuture<Void> firstAcknowledged = new CompletableFuture<>();

        source.send(incomingMessage("first", "acme-in", firstAcknowledged));

        assertEquals(Optional.of("acme-in"), probe.observedTenant().get(5, TimeUnit.SECONDS));
        assertFalse(firstAcknowledged.isDone());
        probe.completeProcessing();
        firstAcknowledged.get(5, TimeUnit.SECONDS);

        probe.reset();
        CompletableFuture<Void> secondAcknowledged = new CompletableFuture<>();
        source.send(incomingMessage("second", null, secondAcknowledged));

        assertEquals(Optional.empty(), probe.observedTenant().get(5, TimeUnit.SECONDS));
        probe.completeProcessing();
        secondAcknowledged.get(5, TimeUnit.SECONDS);
    }

    private static Message<String> incomingMessage(String payload, String tenantId,
            CompletableFuture<Void> acknowledged) {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(INCOMING_CHANNEL, 0, 0L, "key", payload);
        if (tenantId != null) {
            record.headers().add(HEADER, tenantId.getBytes(UTF_8));
        }
        IncomingKafkaRecordMetadata<String, String> metadata = new IncomingKafkaRecordMetadata<>(record, INCOMING_CHANNEL);
        return Message.of(payload, () -> {
            acknowledged.complete(null);
            return CompletableFuture.completedFuture(null);
        }).addMetadata(metadata);
    }

    @Path("/send")
    public static class SenderResource {

        @Inject
        TenantContext tenantContext;

        @Inject
        MessagingProbe probe;

        @GET
        public CompletionStage<String> send(@QueryParam("tenant") String tenantId) {
            tenantContext.setTenantId(tenantId);
            return probe.send("payload").thenApply(ignored -> "sent");
        }
    }

    @ApplicationScoped
    public static class MessagingProbe {

        @Inject
        TenantContext tenantContext;

        @Inject
        @Channel(OUTGOING_CHANNEL)
        Emitter<String> emitter;

        private volatile CompletableFuture<Optional<String>> observedTenant;
        private volatile CompletableFuture<Void> processing;

        void reset() {
            observedTenant = new CompletableFuture<>();
            processing = new CompletableFuture<>();
        }

        CompletableFuture<Optional<String>> observedTenant() {
            return observedTenant.orTimeout(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
        }

        void completeProcessing() {
            processing.complete(null);
        }

        CompletionStage<Void> send(String payload) {
            return emitter.send(payload);
        }

        @Incoming(INCOMING_CHANNEL)
        CompletionStage<Void> receive(String payload) {
            observedTenant.complete(tenantContext.getTenantId());
            return processing;
        }
    }
}
