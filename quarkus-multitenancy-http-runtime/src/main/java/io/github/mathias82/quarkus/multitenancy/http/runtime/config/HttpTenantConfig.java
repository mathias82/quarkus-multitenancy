package io.github.mathias82.quarkus.multitenancy.http.runtime.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.List;

@ConfigMapping(prefix = "quarkus.multitenancy.http")
public interface HttpTenantConfig {

    @WithDefault("false")
    boolean enabled();

    @WithDefault("header")
    List<String> strategy();

    @WithDefault("X-Tenant")
    String headerName();

    @WithDefault("tenant")
    String jwtClaimName();

    @WithDefault("0")
    int pathSegmentIndex();

    @WithDefault("tenant")
    String cookieName();

    @WithDefault("public")
    String defaultTenant();
}
