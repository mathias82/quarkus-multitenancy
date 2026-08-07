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
package io.quarkiverse.multitenancy.messaging.kafka.runtime;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.config.KafkaTenantConfig;
import io.quarkus.arc.Arc;
import io.smallrye.reactive.messaging.OutgoingInterceptor;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;

/**
 * Adds the current tenant to outgoing Kafka message metadata.
 * <p>
 * Explicit {@link OutgoingKafkaRecordMetadata} supplied by the application has
 * precedence: if it already contains the configured header, the interceptor
 * leaves it untouched.
 */
@ApplicationScoped
public class KafkaTenantOutgoingInterceptor implements OutgoingInterceptor {

    private final TenantContext tenantContext;
    private final KafkaTenantConfig config;
    private final Instance<KafkaTenantValidator> validators;

    @Inject
    public KafkaTenantOutgoingInterceptor(TenantContext tenantContext, KafkaTenantConfig config,
            Instance<KafkaTenantValidator> validators) {
        this.tenantContext = tenantContext;
        this.config = config;
        this.validators = validators;
    }

    @Override
    public Message<?> beforeMessageSend(Message<?> message) {
        if (!config.enabled()) {
            return message;
        }

        Optional<OutgoingKafkaRecordMetadata<?>> existing = kafkaMetadata(message);
        if (existing.map(OutgoingKafkaRecordMetadata::getHeaders)
                .map(headers -> headers.lastHeader(config.headerName()))
                .isPresent()) {
            return message;
        }

        Optional<String> tenant = currentTenant();
        if (tenant.isEmpty()) {
            if (config.failOnMissingOutgoingTenant()) {
                throw new KafkaTenantPropagationException(
                        "Cannot send Kafka message without a tenant; either bind TenantContext, add explicit '"
                                + config.headerName() + "' metadata, or disable fail-on-missing-outgoing-tenant");
            }
            return message;
        }

        String tenantId = tenant.orElseThrow();
        validateTenant(tenantId);

        Headers headers = copyHeaders(existing);
        headers.add(new RecordHeader(config.headerName(), tenantId.getBytes(UTF_8)));
        OutgoingKafkaRecordMetadata<?> metadata;
        if (existing.isPresent()) {
            metadata = copyWithHeaders(existing.orElseThrow(), headers);
        } else {
            metadata = OutgoingKafkaRecordMetadata.builder().withHeaders(headers).build();
        }
        return message.addMetadata(metadata);
    }

    private Optional<String> currentTenant() {
        if (!Arc.container().requestContext().isActive()) {
            return Optional.empty();
        }
        return tenantContext.getTenantId();
    }

    private void validateTenant(String tenantId) {
        for (KafkaTenantValidator validator : validators) {
            validator.validate(tenantId).ifPresent(reason -> {
                throw new KafkaTenantPropagationException("Cannot propagate tenant id: " + reason);
            });
        }
    }

    private static Optional<OutgoingKafkaRecordMetadata<?>> kafkaMetadata(Message<?> message) {
        for (Object metadata : message.getMetadata()) {
            if (metadata instanceof OutgoingKafkaRecordMetadata<?> kafkaMetadata) {
                return Optional.of(kafkaMetadata);
            }
        }
        return Optional.empty();
    }

    private static Headers copyHeaders(Optional<OutgoingKafkaRecordMetadata<?>> metadata) {
        return metadata.map(OutgoingKafkaRecordMetadata::getHeaders)
                .map(RecordHeaders::new)
                .map(Headers.class::cast)
                .orElseGet(RecordHeaders::new);
    }

    private static <K> OutgoingKafkaRecordMetadata<K> copyWithHeaders(OutgoingKafkaRecordMetadata<K> metadata,
            Headers headers) {
        return OutgoingKafkaRecordMetadata.from(metadata)
                .withHeaders(headers)
                .build();
    }

    @Override
    public void onMessageAck(Message<?> message) {
        // No outgoing context is retained after the header has been stamped.
    }

    @Override
    public void onMessageNack(Message<?> message, Throwable failure) {
        // No outgoing context is retained after the header has been stamped.
    }
}
