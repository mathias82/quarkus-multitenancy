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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.messaging.kafka.runtime.KafkaTenantIdValidator;
import io.quarkiverse.multitenancy.messaging.kafka.runtime.config.KafkaTenantConfig;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;

class KafkaTenantIdValidatorPolicyTest {

    @Test
    void shouldInheritConfiguredHttpTenantPolicy() {
        var rawConfig = new SmallRyeConfigBuilder()
                .withMapping(KafkaTenantConfig.class)
                .withDefaultValue("quarkus.multi-tenant.http.tenant-id.pattern", "tenant-[0-9]+")
                .withDefaultValue("quarkus.multi-tenant.http.tenant-id.max-length", "16")
                .build();
        KafkaTenantIdValidator validator = new KafkaTenantIdValidator(
                rawConfig.getConfigMapping(KafkaTenantConfig.class), rawConfig);

        assertTrue(validator.validate("tenant-42").isEmpty());
        assertTrue(validator.validate("acme").isPresent());
        assertTrue(validator.validate("tenant-12345678901").isPresent());
    }

    @Test
    void shouldInheritDisabledHttpTenantValidation() {
        var rawConfig = new SmallRyeConfigBuilder()
                .withMapping(KafkaTenantConfig.class)
                .withDefaultValue("quarkus.multi-tenant.http.tenant-id.validation-enabled", "false")
                .build();
        KafkaTenantIdValidator validator = new KafkaTenantIdValidator(
                rawConfig.getConfigMapping(KafkaTenantConfig.class), rawConfig);

        assertTrue(validator.validate("tenant with spaces").isEmpty());
    }

    @Test
    void shouldPreferKafkaSpecificPolicyOverHttpPolicy() {
        var rawConfig = new SmallRyeConfigBuilder()
                .withMapping(KafkaTenantConfig.class)
                .withSources(new PropertiesConfigSource(Map.of(
                        "quarkus.multi-tenant.http.tenant-id.pattern", "http-[0-9]+",
                        "quarkus.multi-tenant.messaging.kafka.tenant-id.pattern", "kafka-[0-9]+"),
                        "application.properties"))
                .build();
        KafkaTenantIdValidator validator = new KafkaTenantIdValidator(
                rawConfig.getConfigMapping(KafkaTenantConfig.class), rawConfig);

        assertTrue(validator.validate("kafka-42").isEmpty());
        assertTrue(validator.validate("http-42").isPresent());
    }
}
