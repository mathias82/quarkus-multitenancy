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

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Locks in the implicit default strategy chain. {@code jwt} is opt-in (issue #15,
 * step 2), so an application that never sets
 * {@code quarkus.multi-tenant.http.strategy} must get {@code header,cookie} — and
 * crucially not {@code jwt}, which would otherwise force JWT verification config.
 */
@QuarkusTest
class DefaultStrategyTest {

    @Inject
    HttpTenantConfig config;

    @Test
    void defaultChainIsHeaderCookieAndExcludesJwt() {
        assertEquals(List.of("header", "cookie"), config.strategy());
    }
}
