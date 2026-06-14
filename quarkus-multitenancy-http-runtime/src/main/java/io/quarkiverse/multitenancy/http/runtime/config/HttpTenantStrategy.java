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
