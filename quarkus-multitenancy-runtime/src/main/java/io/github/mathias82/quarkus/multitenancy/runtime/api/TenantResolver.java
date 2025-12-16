package io.github.mathias82.quarkus.multitenancy.runtime.api;

import java.util.Optional;

/**
 * Strategy interface for resolving the tenant from an HTTP request.
 */
public interface TenantResolver {

    Optional<String> resolve(TenantResolutionContext context);
}
