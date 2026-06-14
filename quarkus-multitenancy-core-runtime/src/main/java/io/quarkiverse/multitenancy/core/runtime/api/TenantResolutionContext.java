package io.quarkiverse.multitenancy.core.runtime.api;

import java.util.Optional;

/**
 * Transport-agnostic carrier for the data a {@link TenantResolver} needs to
 * resolve a tenant. Concrete transports (HTTP, ORM, ...) provide an
 * implementation that exposes their request-scoped objects through
 * {@link #get(Class)}.
 */
public interface TenantResolutionContext {

    /**
     * Returns the contextual object of the requested type, when this context can
     * supply one.
     *
     * @param <T> the requested type
     * @param type the contextual type to look up (for example the JAX-RS
     *        {@code ContainerRequestContext})
     * @return the object of that type, or {@link Optional#empty()} when this
     *         context holds nothing assignable to it
     */
    <T> Optional<T> get(Class<T> type);
}
