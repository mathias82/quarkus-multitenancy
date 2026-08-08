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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.util.Optional;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.core.runtime.context.ReservedTenantIds;
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

    @Test
    void rejectsReservedTenantAtOrmBoundary() {
        OrmTenantResolverAdapter adapter = new OrmTenantResolverAdapter();
        adapter.tenantContext = tenantContextInstance(new TenantContext() {
            @Override
            public Optional<String> getTenantId() {
                return Optional.of(ReservedTenantIds.ORM_BOOTSTRAP);
            }

            @Override
            public void setTenantId(String tenantId) {
            }

            @Override
            public void clear() {
            }
        });

        IllegalStateException ex = assertThrows(IllegalStateException.class, adapter::resolveTenantId);

        assertEquals("Reserved tenant id cannot be used for ORM access: __bootstrap", ex.getMessage());
    }

    @Test
    void bootstrapTenantIsNotARootTenant() {
        assertFalse(resolver.isRoot(OrmTenantResolverAdapter.BOOTSTRAP_TENANT));
    }

    @SuppressWarnings("unchecked")
    private static Instance<TenantContext> tenantContextInstance(TenantContext context) {
        return (Instance<TenantContext>) Proxy.newProxyInstance(
                Instance.class.getClassLoader(),
                new Class<?>[] { Instance.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "get" -> context;
                    case "isUnsatisfied" -> false;
                    case "isAmbiguous" -> false;
                    case "toString" -> "TenantContextInstance";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
