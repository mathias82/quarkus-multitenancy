package io.quarkiverse.multitenancy.http.runtime.config;

import java.util.List;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Runtime configuration for the Quarkus multitenancy HTTP module.
 */
@ConfigMapping(prefix = "quarkus.multi-tenant.http")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface HttpTenantConfig {

    /**
     * Master switch for the HTTP tenant filter. When {@code false} the
     * extension stays on the classpath without resolving a tenant per
     * request.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Ordered list of built-in resolution strategies the HTTP filter
     * will try. Custom (user-defined) {@code TenantResolver} beans run
     * before this chain.
     *
     * <p>
     * Valid values: {@code header}, {@code jwt}, {@code cookie},
     * {@code path}.
     *
     * <p>
     * The default omits {@code path} because path-based tenant
     * resolution typically requires an application-specific URL prefix
     * and is opt-in (see {@link #pathPattern()}).
     */
    @WithDefault("header,jwt,cookie")
    List<String> strategy();

    /**
     * Name of the HTTP header read by {@code HeaderTenantResolver}.
     */
    @WithDefault("X-Tenant")
    String headerName();

    /**
     * Name of the JWT claim read by {@code JwtTenantResolver}.
     */
    @WithDefault("tenant")
    String jwtClaimName();

    /**
     * Name of the HTTP cookie read by {@code CookieTenantResolver}.
     */
    @WithDefault("tenant_cookie")
    String cookieName();

    /**
     * Identifier returned when no resolver yields a tenant.
     */
    @WithDefault("public")
    String defaultTenant();

    /**
     * Regular expression applied to the request path by
     * {@code PathTenantResolver}. The tenant identifier is taken from
     * the capturing group at {@link #pathGroup()}. Only consulted when
     * {@code path} appears in {@link #strategy()}.
     */
    @WithDefault("^/t/([^/]+)(?:/|$)")
    String pathPattern();

    /**
     * Capturing group used by {@link #pathPattern()}.
     */
    @WithDefault("1")
    int pathGroup();
}
