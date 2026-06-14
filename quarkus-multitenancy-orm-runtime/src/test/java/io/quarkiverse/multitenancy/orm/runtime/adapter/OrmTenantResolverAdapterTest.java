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
package io.quarkiverse.multitenancy.orm.runtime.adapter;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OrmTenantResolverAdapterTest {

    @Inject
    @PersistenceUnitExtension
    OrmTenantResolverAdapter resolver;

    @Inject
    TenantContext tenantContext;

    @Test
    void resolvesTenantFromTenantContext() {
        tenantContext.setTenantId("tenant2");

        String tenant = resolver.resolveTenantId();

        assertEquals("tenant2", tenant);
    }

    @Test
    void throwsExceptionWhenTenantNotSet() {
        tenantContext.clear();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                resolver::resolveTenantId);

        assertEquals("Tenant not set before ORM access", ex.getMessage());
    }

    @Test
    void returnsBootstrapTenantWhenNoContextAvailable() {
        tenantContext.clear();

        String tenant = resolver.getDefaultTenantId();

        assertEquals(
                OrmTenantResolverAdapter.BOOTSTRAP_TENANT,
                tenant);
    }
}
