package io.github.mathias82.quarkus.multitenancy.runtime.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;

/**
 * Configuration mapping for multi-tenancy.
 *
 * quarkus.multi-tenant.enabled=true
 * quarkus.multi-tenant.strategy=header
 * quarkus.multi-tenant.header-name=X-Tenant-Id
 * quarkus.multi-tenant.default-tenant=public
 */
@ConfigMapping(prefix = "quarkus.multi-tenant")
public interface MultiTenantConfig {

    @WithDefault("false")
    boolean enabled();

    /**
     * Active tenant resolution strategies (header, jwt, etc).
     */
    @WithDefault("header")
    List<String> strategy();

    /**
     * Header name used when strategy=header.
     */
    @WithDefault("X-Tenant")
    String headerName();

    /**
     * Default tenant id if none is found.
     */
    @WithDefault("public")
    String defaultTenant();

    /**
     * Claim name for JWT strategy.
     */
    @WithDefault("tenant")
    String jwtClaimName();
}

