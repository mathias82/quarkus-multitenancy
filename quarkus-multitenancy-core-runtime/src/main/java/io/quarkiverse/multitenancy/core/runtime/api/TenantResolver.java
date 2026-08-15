/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.multitenancy.core.runtime.api;

import jakarta.annotation.Priority;

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
 *
 * <p>
 * When multiple resolvers are available, applications can use
 * {@link Priority @Priority} to define their order. Higher numeric values run
 * first. Resolvers without an explicit annotation use
 * {@link #DEFAULT_PRIORITY}. Equal-priority resolvers are ordered by their
 * implementation class name.
 */
public interface TenantResolver {

    /**
     * Priority assigned by Quarkus Arc when a resolver has no explicit
     * {@link Priority @Priority} annotation.
     */
    int DEFAULT_PRIORITY = 0;

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
