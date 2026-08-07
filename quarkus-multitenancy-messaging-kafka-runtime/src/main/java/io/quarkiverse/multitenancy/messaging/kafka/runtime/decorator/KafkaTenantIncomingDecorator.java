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
import io.quarkiverse.multitenancy.messaging.kafka.runtime.interceptor.KafkaTenantIncomingInterceptor;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.interceptor.KafkaTenantInterceptorBinding;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.PublisherDecorator;

/**
 * Applies incoming tenant propagation only to Kafka connector channels and
 * isolates validation failures to the rejected message.
 */
@ApplicationScoped
public class KafkaTenantIncomingDecorator implements PublisherDecorator {

    private static final int AFTER_REQUEST_CONTEXT_ACTIVATION = 400;

    private final KafkaConnectorChannels channels;
    private final KafkaTenantIncomingInterceptor interceptor;

    @Inject
    public KafkaTenantIncomingDecorator(KafkaConnectorChannels channels,
            @KafkaTenantInterceptorBinding KafkaTenantIncomingInterceptor interceptor) {
        this.channels = channels;
        this.interceptor = interceptor;
    }

    @Override
    public Multi<? extends Message<?>> decorate(Multi<? extends Message<?>> publisher,
            List<String> channelNames, boolean isConnector) {
        if (!isConnector || !channels.isIncomingKafka(channelNames)) {
            return publisher;
        }
        return publisher.onItem().transformToMultiAndConcatenate(this::interceptOrNack);
    }

    private Multi<? extends Message<?>> interceptOrNack(Message<?> message) {
        try {
            return Multi.createFrom().item(interceptor.afterMessageReceive(message));
        } catch (RuntimeException failure) {
            return Uni.createFrom()
                    .completionStage(() -> message.nack(failure))
                    .onItem().<Message<?>> transformToMulti(ignored -> Multi.createFrom().empty());
        }
    }

    @Override
    public int getPriority() {
        return AFTER_REQUEST_CONTEXT_ACTIVATION;
    }
}
