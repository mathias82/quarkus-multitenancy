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
package io.quarkiverse.multitenancy.messaging.kafka.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Runtime configuration for tenant propagation over Kafka messages.
 */
@ConfigMapping(prefix = "quarkus.multi-tenant.messaging.kafka")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface KafkaTenantConfig {

    /**
     * Whether automatic incoming and outgoing Kafka tenant propagation is
     * enabled.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Kafka record header used to carry the tenant identifier.
     */
    @WithDefault("X-Tenant")
    String headerName();

    /**
     * Whether an incoming Kafka message without the tenant header is rejected.
     * By default the handler runs with an empty {@code TenantContext}.
     */
    @WithDefault("false")
    boolean failOnMissingIncomingTenant();

    /**
     * Whether an outgoing Kafka message is rejected when neither explicit
     * tenant metadata nor a current tenant is available.
     */
    @WithDefault("false")
    boolean failOnMissingOutgoingTenant();

    /**
     * Validation applied to untrusted tenant identifiers read from Kafka.
     */
    TenantIdConfig tenantId();

    /**
     * Length and character-set policy for a propagated tenant identifier.
     */
    interface TenantIdConfig {

        /**
         * Whether tenant identifiers are checked against the configured length
         * and pattern.
         */
        @WithDefault("true")
        boolean validationEnabled();

        /**
         * Maximum number of characters accepted in a tenant identifier.
         */
        @WithDefault("64")
        int maxLength();

        /**
         * Regular expression a tenant identifier must match in full.
         */
        @WithDefault("[A-Za-z0-9_-]+")
        String pattern();
    }
}
