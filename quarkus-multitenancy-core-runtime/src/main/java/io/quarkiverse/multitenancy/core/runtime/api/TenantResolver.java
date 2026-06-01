package io.quarkiverse.multitenancy.core.runtime.api;

import java.util.Optional;

/**
 * Strategy interface for resolving the tenant.
 */
public interface TenantResolver {

    Optional<String> resolve(TenantResolutionContext context);

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
