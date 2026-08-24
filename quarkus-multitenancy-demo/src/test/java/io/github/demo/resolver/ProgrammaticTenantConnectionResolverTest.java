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
package io.github.demo.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import jakarta.inject.Inject;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.junit.jupiter.api.Test;

import io.github.demo.exception.TenantNotFoundException;
import io.github.demo.resource.config.ProgrammaticH2Profile;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(ProgrammaticH2Profile.class)
class ProgrammaticTenantConnectionResolverTest {

    @Inject
    TenantConnectionCatalog catalog;

    @Inject
    @PersistenceUnitExtension
    ProgrammaticTenantConnectionResolver resolver;

    @Test
    void exposesConfiguredTenants() {
        assertEquals(Set.of("tenant1", "tenant2"), catalog.tenantIds());
    }

    @Test
    void returnsOneStableProviderPerTenant() {
        ConnectionProvider tenant1 = resolver.resolve("tenant1");

        assertSame(tenant1, resolver.resolve("tenant1"));
        assertNotSame(tenant1, resolver.resolve("tenant2"));
        assertEquals(2, resolver.createdProviderCount());
    }

    @Test
    void rejectsUnknownTenantWithoutCreatingPool() {
        int providersBeforeResolution = resolver.createdProviderCount();

        assertThrows(TenantNotFoundException.class, () -> resolver.resolve("missing"));
        assertEquals(providersBeforeResolution, resolver.createdProviderCount());
    }
}
