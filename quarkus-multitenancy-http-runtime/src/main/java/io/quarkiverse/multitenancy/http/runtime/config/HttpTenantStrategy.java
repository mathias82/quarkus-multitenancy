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
package io.quarkiverse.multitenancy.http.runtime.config;

import java.util.Locale;
import java.util.Optional;

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
    cookie;

    /**
     * Parses a single {@code quarkus.multi-tenant.http.strategy} entry into a
     * strategy. The value is trimmed and lower-cased first, so configuration
     * such as {@code Header} or {@code JWT} is accepted, matching the
     * case-insensitive behaviour users get from other Quarkus config.
     *
     * <p>
     * This is the single source of truth for what counts as a known strategy
     * name: both the request filter and the startup validator parse through it,
     * so they can never disagree. A {@code null}, blank or unknown value yields
     * an empty {@link Optional}; callers distinguish a blank entry (to skip)
     * from a non-blank unknown (to reject) by checking the raw value first.
     *
     * @param value a raw configured strategy name
     * @return the matching strategy, or empty if blank or unrecognised
     */
    public static Optional<HttpTenantStrategy> fromConfigValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(normalized));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
