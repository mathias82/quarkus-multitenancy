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
         * Whether the ORM-module header fallback is enabled.
         *
         * <p>
         * When {@code true} (the default), the filter resolves the tenant from
         * the configured header only when no earlier resolution path has already
         * populated {@code TenantContext}. This preserves ORM-only header-based
         * resolution while allowing the HTTP tenant-resolution pipeline to remain
         * authoritative when both modules are installed.
         *
         * <p>
         * Set this to {@code false} only when the application does not want the ORM
         * module to provide a header-based fallback at all.
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
