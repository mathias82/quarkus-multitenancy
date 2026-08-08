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
package io.quarkiverse.multitenancy.orm.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Runtime configuration for the Quarkus multitenancy ORM module.
 */
@ConfigMapping(prefix = "quarkus.multi-tenant.orm")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OrmTenantConfig {

    /**
     * Configuration for the JAX-RS request filter that resolves the
     * tenant from the {@code X-Tenant} header.
     */
    HeaderFilterConfig headerFilter();

    /**
     * Tenant-id validation used by the ORM-only header fallback.
     */
    TenantIdConfig tenantId();

    /**
     * Configuration for the ORM module's {@code X-Tenant} header filter.
     */
    interface HeaderFilterConfig {

        /**
         * Whether the ORM-module {@code X-Tenant} header filter is
         * registered.
         *
         * <p>
         * When {@code true} (the default), every JAX-RS request must
         * carry the {@code X-Tenant} header and the filter aborts with
         * HTTP 400 otherwise. This preserves the historical behaviour
         * for applications that rely on header-based tenant resolution.
         *
         * <p>
         * Set this to {@code false} when the HTTP-side tenant
         * resolution is driven by {@code quarkus-multitenancy-http}
         * (with any of the {@code path}, {@code jwt}, or {@code cookie}
         * strategies) so the two filters do not collide on the request
         * lifecycle. With the ORM filter disabled the resolved tenant
         * still flows into Hibernate ORM through
         * {@code OrmTenantResolverAdapter}, which only requires the
         * shared {@code TenantContext} to be populated upstream.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * Name of the HTTP header read by the ORM-only tenant fallback.
         *
         * <p>
         * The fallback is skipped when {@code TenantContext} has already been
         * populated by an earlier tenant-resolution path.
         */
        @WithDefault("X-Tenant")
        String headerName();
    }

    interface TenantIdConfig {
        /**
         * Whether ORM-only resolved tenant identifiers are validated against
         * the configured maximum length and pattern.
         */
        @WithDefault("true")
        boolean validationEnabled();

        /**
         * Maximum number of characters allowed in an ORM-only resolved tenant
         * identifier.
         */
        @WithDefault("64")
        int maxLength();

        /**
         * Regular expression an ORM-only resolved tenant identifier must match
         * in full before it is published to {@code TenantContext}.
         */
        @WithDefault("[A-Za-z0-9_-]+")
        String pattern();
    }
}
