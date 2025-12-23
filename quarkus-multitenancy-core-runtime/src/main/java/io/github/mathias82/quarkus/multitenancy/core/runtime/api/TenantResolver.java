package io.github.mathias82.quarkus.multitenancy.core.runtime.api;

import java.util.Optional;

/**
 * Strategy interface for resolving the tenant.
 */
public interface TenantResolver {
    Optional<String> resolve(TenantResolutionContext context);
}
