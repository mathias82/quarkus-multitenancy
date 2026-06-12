package io.quarkiverse.multitenancy.http.runtime.config;

/**
 * Built-in HTTP tenant resolution strategies. The active chain and its order are
 * configured through {@code quarkus.multi-tenant.http.strategy}; each value names
 * the resolver that handles it.
 */
public enum HttpTenantStrategy {
    /** Resolve the tenant from a request header (default {@code X-Tenant}). */
    header,
    /** Resolve the tenant from a verified JWT claim (default {@code tenant}). */
    jwt,
    /** Resolve the tenant from a path segment matched by the configured pattern. */
    path,
    /** Resolve the tenant from a request cookie (default {@code tenant_cookie}). */
    cookie
}
