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
package io.quarkiverse.multitenancy.messaging.kafka.runtime.channel;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;

/**
 * Identifies channels backed by the SmallRye Kafka connector.
 * <p>
 * Channel decorators are connector-aware but do not receive the connector
 * name. Resolving it from the channel configuration prevents Kafka metadata
 * from being attached to AMQP, in-memory, or custom connector messages.
 */
@ApplicationScoped
public class KafkaConnectorChannels {

    private static final String KAFKA_CONNECTOR = "smallrye-kafka";
    private static final String KAFKA_CONNECTOR_ALIAS = "kafka";
    private static final char CONCURRENCY_SEPARATOR = '$';
    private static final String INCOMING_PREFIX = "mp.messaging.incoming.";
    private static final String OUTGOING_PREFIX = "mp.messaging.outgoing.";

    private final Config config;

    @Inject
    public KafkaConnectorChannels(Config config) {
        this.config = config;
    }

    public boolean isIncomingKafka(String channelName) {
        return isKafka(INCOMING_PREFIX, baseConcurrencyChannelName(channelName));
    }

    public boolean isIncomingKafka(List<String> channelNames) {
        return !channelNames.isEmpty()
                && channelNames.stream().allMatch(this::isIncomingKafka);
    }

    public boolean isOutgoingKafka(List<String> channelNames) {
        return !channelNames.isEmpty()
                && channelNames.stream().allMatch(channelName -> isKafka(OUTGOING_PREFIX, channelName));
    }

    private boolean isKafka(String directionPrefix, String channelName) {
        if (channelName == null) {
            return false;
        }
        return connector(directionPrefix, channelName)
                .filter(KafkaConnectorChannels::isKafkaConnector)
                .isPresent();
    }

    private static String baseConcurrencyChannelName(String channelName) {
        if (channelName == null) {
            return null;
        }
        int separator = channelName.indexOf(CONCURRENCY_SEPARATOR);
        return separator < 0 ? channelName : channelName.substring(0, separator);
    }

    private static boolean isKafkaConnector(String connector) {
        return KAFKA_CONNECTOR.equals(connector) || KAFKA_CONNECTOR_ALIAS.equals(connector);
    }

    private Optional<String> connector(String directionPrefix, String channelName) {
        String channelPrefix = channelName.contains(".")
                ? directionPrefix + '"' + channelName + "\"."
                : directionPrefix + channelName + '.';
        return config.getOptionalValue(channelPrefix + "connector", String.class)
                .or(() -> config.getOptionalValue(channelPrefix + "type", String.class));
    }
}
