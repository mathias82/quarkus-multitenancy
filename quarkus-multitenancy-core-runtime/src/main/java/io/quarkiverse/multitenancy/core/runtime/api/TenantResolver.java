package io.quarkiverse.multitenancy.core.runtime.api;

/**
 * Strategy interface for resolving the tenant.
 *
 * <p>
 * Implementations return a {@link TenantResolution} describing one of three
 * outcomes — see the contract on that type. In particular, a resolver that
 * processes input it considers invalid (for example, a JWT it cannot verify)
 * must return {@link TenantResolution#rejected(String)} rather than
 * {@link TenantResolution#notApplicable()}, so the dispatcher can abort the
 * request with 401 instead of silently falling back to the default tenant.
 */
public interface TenantResolver {

    /**
     * Resolve the tenant for the current request.
     *
     * @param context resolution context (transport-specific data such as the
     *        JAX-RS request)
     * @return the resolution outcome; never {@code null}
     */
    TenantResolution resolve(TenantResolutionContext context);

    /**
     * Identifies the resolution strategy this resolver implements (e.g. "header",
     * "jwt", "cookie", "path"). Returning an empty string marks the resolver as a
     * user-defined custom resolver, which transport-aware dispatchers may treat
     * differently from built-ins (for example, run them before the configured
     * strategy chain).
     */
    default String name() {
        return "";
    }
}
