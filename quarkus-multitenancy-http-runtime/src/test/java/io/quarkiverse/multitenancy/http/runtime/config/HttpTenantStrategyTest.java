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
package io.quarkiverse.multitenancy.http.runtime.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Pins the single source of truth for parsing a configured strategy name, used
 * by both the request filter and the startup validator.
 */
class HttpTenantStrategyTest {

    @Test
    void parsesCanonicalLowercaseNames() {
        assertEquals(Optional.of(HttpTenantStrategy.header), HttpTenantStrategy.fromConfigValue("header"));
        assertEquals(Optional.of(HttpTenantStrategy.jwt), HttpTenantStrategy.fromConfigValue("jwt"));
        assertEquals(Optional.of(HttpTenantStrategy.cookie), HttpTenantStrategy.fromConfigValue("cookie"));
        assertEquals(Optional.of(HttpTenantStrategy.path), HttpTenantStrategy.fromConfigValue("path"));
    }

    @Test
    void parsesMixedCaseAndSurroundingWhitespace() {
        assertEquals(Optional.of(HttpTenantStrategy.header), HttpTenantStrategy.fromConfigValue("Header"));
        assertEquals(Optional.of(HttpTenantStrategy.jwt), HttpTenantStrategy.fromConfigValue("JWT"));
        assertEquals(Optional.of(HttpTenantStrategy.cookie), HttpTenantStrategy.fromConfigValue("  Cookie  "));
    }

    @Test
    void returnsEmptyForUnknownName() {
        assertTrue(HttpTenantStrategy.fromConfigValue("heder").isEmpty());
    }

    @Test
    void returnsEmptyForBlankOrNull() {
        assertTrue(HttpTenantStrategy.fromConfigValue("").isEmpty());
        assertTrue(HttpTenantStrategy.fromConfigValue("   ").isEmpty());
        assertTrue(HttpTenantStrategy.fromConfigValue(null).isEmpty());
    }
}
