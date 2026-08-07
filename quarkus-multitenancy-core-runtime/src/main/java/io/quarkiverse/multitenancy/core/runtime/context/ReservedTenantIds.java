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

/**
 * Tenant identifiers reserved for internal extension use.
 */
public final class ReservedTenantIds {

    /**
     * Internal tenant used while Hibernate ORM starts without a request tenant.
     */
    public static final String ORM_BOOTSTRAP = "__bootstrap";

    private ReservedTenantIds() {
    }

    /**
     * Checks whether an identifier is reserved for internal use.
     *
     * @param tenantId tenant identifier to check
     * @return {@code true} when applications must not select the identifier
     */
    public static boolean isReserved(String tenantId) {
        return ORM_BOOTSTRAP.equals(tenantId);
    }
}
