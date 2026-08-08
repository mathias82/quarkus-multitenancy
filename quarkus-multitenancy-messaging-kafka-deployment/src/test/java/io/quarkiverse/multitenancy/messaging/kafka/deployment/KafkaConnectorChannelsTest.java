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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.messaging.kafka.runtime.channel.KafkaConnectorChannels;
import io.smallrye.config.SmallRyeConfigBuilder;

class KafkaConnectorChannelsTest {

    @Test
    void shouldIdentifyOnlyKafkaConnectorChannels() {
        var config = new SmallRyeConfigBuilder()
                .withDefaultValue("mp.messaging.incoming.kafka-in.connector", "smallrye-kafka")
                .withDefaultValue("mp.messaging.incoming.memory-in.connector", "smallrye-in-memory")
                .withDefaultValue("mp.messaging.outgoing.kafka-out.connector", "smallrye-kafka")
                .withDefaultValue("mp.messaging.outgoing.memory-out.connector", "smallrye-in-memory")
                .build();
        KafkaConnectorChannels channels = new KafkaConnectorChannels(config);

        assertTrue(channels.isIncomingKafka("kafka-in"));
        assertFalse(channels.isIncomingKafka("memory-in"));
        assertFalse(channels.isIncomingKafka("missing"));
        assertTrue(channels.isIncomingKafka(List.of("kafka-in")));
        assertFalse(channels.isIncomingKafka(List.of("kafka-in", "memory-in")));
        assertTrue(channels.isOutgoingKafka(List.of("kafka-out")));
        assertFalse(channels.isOutgoingKafka(List.of("memory-out")));
        assertFalse(channels.isOutgoingKafka(List.of("kafka-out", "memory-out")));
        assertFalse(channels.isOutgoingKafka(List.of()));
    }

    @Test
    void shouldResolveQuotedChannelNamesAndLegacyTypeAttribute() {
        var config = new SmallRyeConfigBuilder()
                .withDefaultValue("mp.messaging.incoming.\"orders.eu\".connector", "smallrye-kafka")
                .withDefaultValue("mp.messaging.outgoing.\"events.eu\".type", "smallrye-kafka")
                .build();
        KafkaConnectorChannels channels = new KafkaConnectorChannels(config);

        assertTrue(channels.isIncomingKafka("orders.eu"));
        assertTrue(channels.isOutgoingKafka(List.of("events.eu")));
    }

    @Test
    void shouldResolveIndexedConcurrencyChannelNamesFromBaseChannelConfiguration() {
        var config = new SmallRyeConfigBuilder()
                .withDefaultValue("mp.messaging.incoming.orders.connector", "smallrye-kafka")
                .withDefaultValue("mp.messaging.incoming.\"orders.eu\".connector", "smallrye-kafka")
                .build();
        KafkaConnectorChannels channels = new KafkaConnectorChannels(config);

        assertTrue(channels.isIncomingKafka("orders$1"));
        assertTrue(channels.isIncomingKafka(List.of("orders$1", "orders$2")));
        assertTrue(channels.isIncomingKafka("orders.eu$1"));
        assertFalse(channels.isOutgoingKafka(List.of("orders$1")));
    }

    @Test
    void shouldRecognizeKafkaConnectorAlias() {
        var config = new SmallRyeConfigBuilder()
                .withDefaultValue("mp.messaging.incoming.kafka-in.connector", "kafka")
                .withDefaultValue("mp.messaging.outgoing.kafka-out.connector", "kafka")
                .build();
        KafkaConnectorChannels channels = new KafkaConnectorChannels(config);

        assertTrue(channels.isIncomingKafka("kafka-in"));
        assertTrue(channels.isOutgoingKafka(List.of("kafka-out")));
    }
}
