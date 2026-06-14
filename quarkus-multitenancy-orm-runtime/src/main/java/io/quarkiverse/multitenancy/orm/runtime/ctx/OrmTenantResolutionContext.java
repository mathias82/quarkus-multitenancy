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
package io.quarkiverse.multitenancy.orm.runtime.ctx;

import java.util.Optional;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;

/**
 * ORM-specific TenantResolutionContext that may later include ORM-related metadata.
 */
public class OrmTenantResolutionContext implements TenantResolutionContext {

    private final String tenantId;

    public OrmTenantResolutionContext(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public <T> Optional<T> get(Class<T> type) {
        if (type.equals(String.class)) {
            return Optional.of(type.cast(tenantId));
        }
        return Optional.empty();
    }
}
