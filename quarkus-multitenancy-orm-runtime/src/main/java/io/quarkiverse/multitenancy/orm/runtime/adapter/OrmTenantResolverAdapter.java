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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;

/**
 * Bridges the shared {@link TenantContext} into Hibernate ORM's tenant
 * resolution SPI. Registered as a {@code @PersistenceUnitExtension}, it hands
 * Hibernate the tenant currently held in the request-scoped context, falling
 * back to {@link #BOOTSTRAP_TENANT} when no context is active (for example
 * during schema bootstrap, outside of a request).
 */
@PersistenceUnitExtension
@ApplicationScoped
public class OrmTenantResolverAdapter implements io.quarkus.hibernate.orm.runtime.tenant.TenantResolver {

    /**
     * Tenant id used when no request-scoped tenant is available, such as during
     * Hibernate's bootstrap and schema handling.
     */
    public static final String BOOTSTRAP_TENANT = "__bootstrap";

    @Inject
    Instance<TenantContext> tenantContext;

    @Override
    public String getDefaultTenantId() {
        return BOOTSTRAP_TENANT;
    }

    @Override
    public String resolveTenantId() {
        if (tenantContext == null || tenantContext.isUnsatisfied()) {
            return BOOTSTRAP_TENANT;
        }

        return tenantContext.get()
                .getTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not set before ORM access"));
    }

    @Override
    public boolean isRoot(String tenantId) {
        return BOOTSTRAP_TENANT.equals(tenantId);
    }
}
