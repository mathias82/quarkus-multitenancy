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

import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaConnectorChannels;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantIdValidator;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantIncomingDecorator;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantIncomingInterceptor;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantOutgoingDecorator;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantOutgoingInterceptor;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.config.KafkaTenantConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConfigMappingBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class KafkaTenantProcessor {

    private static final String FEATURE = "quarkus-multitenancy-messaging-kafka";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ConfigMappingBuildItem kafkaTenantConfig() {
        return new ConfigMappingBuildItem(KafkaTenantConfig.class, "quarkus.multi-tenant.messaging.kafka");
    }

    @BuildStep
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(KafkaConnectorChannels.class)
                .addBeanClass(KafkaTenantIdValidator.class)
                .addBeanClass(KafkaTenantIncomingDecorator.class)
                .addBeanClass(KafkaTenantIncomingInterceptor.class)
                .addBeanClass(KafkaTenantOutgoingDecorator.class)
                .addBeanClass(KafkaTenantOutgoingInterceptor.class)
                .setUnremovable()
                .build());
    }
}
