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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantIncomingInterceptor;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantOutgoingInterceptor;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantPropagationException;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;

class StrictKafkaTenantPropagationTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.kafka.devservices.enabled", "false")
            .overrideConfigKey("quarkus.multi-tenant.messaging.kafka.fail-on-missing-incoming-tenant", "true")
            .overrideConfigKey("quarkus.multi-tenant.messaging.kafka.fail-on-missing-outgoing-tenant", "true");

    @Inject
    TenantContext tenantContext;

    @Inject
    KafkaTenantOutgoingInterceptor outgoing;

    @Inject
    KafkaTenantIncomingInterceptor incoming;

    @Test
    @ActivateRequestContext
    void shouldRejectMissingIncomingTenantInStrictMode() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("tenant-in", 0, 0L, "key", "payload");
        IncomingKafkaRecordMetadata<String, String> metadata = new IncomingKafkaRecordMetadata<>(record, "tenant-in");
        Message<String> message = Message.of("payload", () -> CompletableFuture.completedFuture(null))
                .addMetadata(metadata);

        assertThrows(KafkaTenantPropagationException.class, () -> incoming.afterMessageReceive(message));
    }

    @Test
    @ActivateRequestContext
    void shouldRejectMissingOutgoingTenantInStrictMode() {
        tenantContext.clear();

        assertThrows(KafkaTenantPropagationException.class,
                () -> outgoing.beforeMessageSend(Message.of("payload")));
    }
}
