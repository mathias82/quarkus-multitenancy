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
package io.quarkiverse.multitenancy.core.runtime.context;

import java.util.Optional;

/**
 * Represents the per-request tenant context.
 */
public interface TenantContext {

    /**
     * Returns the tenant bound to the current request, when one has been set.
     *
     * @return the current tenant id, or {@link Optional#empty()} when no tenant
     *         has been resolved yet
     */
    Optional<String> getTenantId();

    /**
     * Binds a tenant to the current request, replacing any previously set value.
     *
     * @param tenantId the tenant id to associate with the current request
     */
    void setTenantId(String tenantId);

    /**
     * Clears the tenant bound to the current request.
     */
    void clear();
}
