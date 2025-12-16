package io.github.mathias82.quarkus.multitenancy.core.runtime.config;

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
     * Enabled tenant resolution strategies.
     * Example: header, jwt, path, cookie
     */
    @WithDefault("header")
    List<String> strategy();

    @WithDefault("public")
    String defaultTenant();
}

