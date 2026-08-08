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
package io.quarkiverse.multitenancy.messaging.kafka.runtime.decorator;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.multitenancy.messaging.kafka.runtime.channel.KafkaConnectorChannels;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.interceptor.KafkaTenantInterceptorBinding;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.interceptor.KafkaTenantOutgoingInterceptor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.SubscriberDecorator;

/**
 * Captures the tenant on the producing thread before Quarkus clears connector
 * context propagation.
 * <p>
 * The regular outgoing interceptor still runs immediately before transmission,
 * but by then request-scoped state is intentionally unavailable on connector
 * channels. Decorating before Quarkus' connector context boundary ensures an
 * emitter invoked from an HTTP request can stamp the tenant while that request
 * context is still active.
 */
@ApplicationScoped
public class KafkaTenantOutgoingDecorator implements SubscriberDecorator {

    private static final int BEFORE_CONNECTOR_CONTEXT_CLEARING = -200;

    private final KafkaConnectorChannels channels;
    private final KafkaTenantOutgoingInterceptor interceptor;

    @Inject
    public KafkaTenantOutgoingDecorator(KafkaConnectorChannels channels,
            @KafkaTenantInterceptorBinding KafkaTenantOutgoingInterceptor interceptor) {
        this.channels = channels;
        this.interceptor = interceptor;
    }

    @Override
    public Multi<? extends Message<?>> decorate(Multi<? extends Message<?>> publisher,
            List<String> channelNames, boolean isConnector) {
        if (!isConnector || !channels.isOutgoingKafka(channelNames)) {
            return publisher;
        }
        return publisher.onItem().transformToMultiAndConcatenate(this::interceptOrNack);
    }

    private Multi<? extends Message<?>> interceptOrNack(Message<?> message) {
        try {
            return Multi.createFrom().item(interceptor.beforeMessageSend(message));
        } catch (RuntimeException failure) {
            return Uni.createFrom()
                    .completionStage(() -> message.nack(failure))
                    .onItem().<Message<?>> transformToMulti(ignored -> Multi.createFrom().empty());
        }
    }

    @Override
    public int getPriority() {
        return BEFORE_CONNECTOR_CONTEXT_CLEARING;
    }
}
