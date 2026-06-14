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
    }
}
