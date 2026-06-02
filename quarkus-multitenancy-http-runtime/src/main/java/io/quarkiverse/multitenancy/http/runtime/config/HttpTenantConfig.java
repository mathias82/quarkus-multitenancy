package io.quarkiverse.multitenancy.http.runtime.config;

import java.util.List;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.multi-tenant.http")
public interface HttpTenantConfig {

    @WithDefault("true")
    boolean enabled();

    @WithDefault("header")
    List<String> strategy();

    @WithDefault("X-Tenant")
    String headerName();

    @WithDefault("tenant")
    String jwtClaimName();

    @WithDefault("tenant_cookie")
    String cookieName();

    @WithDefault("public")
    String defaultTenant();
}
