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
package io.quarkiverse.multitenancy.messaging.kafka.runtime.validation;

import java.util.Optional;

/**
 * Validates a tenant identifier before it crosses a Kafka messaging boundary.
 * <p>
 * The extension provides a validator for the configured syntax and length
 * policy. Applications may add CDI beans implementing this interface to apply
 * domain checks, such as rejecting identifiers that are not present in a
 * tenant registry. Every available validator must accept the identifier.
 */
@FunctionalInterface
public interface KafkaTenantValidator {

    /**
     * Validates a tenant identifier.
     *
     * @param tenantId non-null, decoded tenant identifier
     * @return a log-safe rejection reason, or empty when accepted
     */
    Optional<String> validate(String tenantId);
}
