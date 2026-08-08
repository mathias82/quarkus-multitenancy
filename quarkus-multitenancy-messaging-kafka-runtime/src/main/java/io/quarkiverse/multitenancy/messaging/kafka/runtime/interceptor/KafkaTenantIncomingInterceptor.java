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
package io.quarkiverse.multitenancy.messaging.kafka.runtime.interceptor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.apache.kafka.common.header.Header;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.config.KafkaTenantConfig;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.exception.KafkaTenantPropagationException;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.validation.KafkaTenantValidator;
import io.quarkus.arc.Arc;
import io.smallrye.reactive.messaging.IncomingInterceptor;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;

/**
 * Establishes {@link TenantContext} from incoming Kafka record metadata.
 * <p>
 * The tenant is restored immediately before acknowledgment or negative
 * acknowledgment delegates to the connector. This keeps it available for an
 * asynchronous handler while allowing Quarkus Messaging to own and destroy
 * the request context at the end of the message lifecycle.
 */
@ApplicationScoped
@KafkaTenantInterceptorBinding
public class KafkaTenantIncomingInterceptor implements IncomingInterceptor {

    private static final String REQUEST_SCOPE_PROPERTY = "quarkus.messaging.request-scoped.enabled=true";

    private final TenantContext tenantContext;
    private final KafkaTenantConfig config;
    private final Instance<KafkaTenantValidator> validators;

    @Inject
    public KafkaTenantIncomingInterceptor(TenantContext tenantContext, KafkaTenantConfig config,
            Instance<KafkaTenantValidator> validators) {
        this.tenantContext = tenantContext;
        this.config = config;
        this.validators = validators;
    }

    @Override
    public Message<?> afterMessageReceive(Message<?> message) {
        if (!config.enabled()) {
            return message;
        }

        Optional<IncomingKafkaRecordMetadata<?, ?>> kafkaMetadata = kafkaMetadata(message);
        if (kafkaMetadata.isEmpty()) {
            return message;
        }

        Optional<String> incomingTenant = readTenant(kafkaMetadata.orElseThrow());
        ensureRequestContextActive();

        Optional<String> previousTenant = tenantContext.getTenantId();
        incomingTenant.ifPresentOrElse(tenantContext::setTenantId, tenantContext::clear);
        return restoreTenantOnCompletion(message, previousTenant);
    }

    private Optional<String> readTenant(IncomingKafkaRecordMetadata<?, ?> metadata) {
        Header header = metadata.getHeaders().lastHeader(config.headerName());
        if (header == null) {
            if (config.failOnMissingIncomingTenant()) {
                throw new KafkaTenantPropagationException(
                        "Incoming Kafka message is missing required tenant header '" + config.headerName() + "'");
            }
            return Optional.empty();
        }

        String tenantId = decode(header);
        validateTenant(tenantId);
        return Optional.of(tenantId);
    }

    private static Optional<IncomingKafkaRecordMetadata<?, ?>> kafkaMetadata(Message<?> message) {
        for (Object metadata : message.getMetadata()) {
            if (metadata instanceof IncomingKafkaRecordMetadata<?, ?> kafkaMetadata) {
                return Optional.of(kafkaMetadata);
            }
        }
        return Optional.empty();
    }

    private void validateTenant(String tenantId) {
        for (KafkaTenantValidator validator : validators) {
            validator.validate(tenantId).ifPresent(reason -> {
                throw new KafkaTenantPropagationException("Rejecting tenant id from Kafka header '"
                        + config.headerName() + "': " + reason);
            });
        }
    }

    private String decode(Header header) {
        if (header.value() == null) {
            throw new KafkaTenantPropagationException(
                    "Kafka tenant header '" + config.headerName() + "' has a null value");
        }
        try {
            return UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(header.value()))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new KafkaTenantPropagationException(
                    "Kafka tenant header '" + config.headerName() + "' is not valid UTF-8",
                    e);
        }
    }

    private void ensureRequestContextActive() {
        if (!Arc.container().requestContext().isActive()) {
            throw new KafkaTenantPropagationException(
                    "Incoming Kafka tenant propagation requires " + REQUEST_SCOPE_PROPERTY);
        }
    }

    private Message<?> restoreTenantOnCompletion(Message<?> message, Optional<String> previousTenant) {
        AtomicBoolean restored = new AtomicBoolean();
        Runnable restore = () -> {
            if (restored.compareAndSet(false, true) && Arc.container().requestContext().isActive()) {
                previousTenant.ifPresentOrElse(tenantContext::setTenantId, tenantContext::clear);
            }
        };

        Message<?> withAck = message.withAckWithMetadata(metadata -> {
            restore.run();
            return message.ack(metadata);
        });
        return withAck.withNackWithMetadata((reason, metadata) -> {
            restore.run();
            return withAck.nack(reason, metadata);
        });
    }

    @Override
    public void onMessageAck(Message<?> message) {
        // Restoration happens before the connector acknowledgment destroys the request context.
    }

    @Override
    public void onMessageNack(Message<?> message, Throwable failure) {
        // Restoration happens before the connector acknowledgment destroys the request context.
    }
}
