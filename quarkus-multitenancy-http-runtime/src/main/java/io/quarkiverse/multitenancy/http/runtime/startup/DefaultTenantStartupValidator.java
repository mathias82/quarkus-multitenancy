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
package io.quarkiverse.multitenancy.http.runtime.startup;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.validation.TenantIdValidator;
import io.quarkus.runtime.StartupEvent;

/**
 * Fails fast at startup when the configured fallback tenant violates the
 * active tenant-id policy.
 *
 * <p>
 * A resolver-produced identifier is checked against
 * {@code quarkus.multi-tenant.http.tenant-id.max-length} and
 * {@code .pattern} by the HTTP filter before it reaches the
 * {@code TenantContext}. The configured
 * {@link HttpTenantConfig#defaultTenant() default tenant} takes the same path
 * into the {@code TenantContext} whenever no resolver matches, yet it was never
 * subjected to that policy. That allowed a configuration such as
 *
 * <pre>
 * quarkus.multi-tenant.http.default-tenant=my@tenant
 * quarkus.multi-tenant.http.tenant-id.pattern=[A-Za-z0-9_-]+
 * </pre>
 *
 * to boot cleanly and then publish a policy-violating identifier on every
 * unresolved request.
 *
 * <p>
 * This validator closes that gap by running the default tenant through the
 * very same {@link TenantIdValidator} the filter uses, so the two paths share a
 * single policy definition. Configurable length and pattern validation may be
 * turned off, but identifiers reserved for internal extension use are always
 * rejected.
 */
@ApplicationScoped
public class DefaultTenantStartupValidator {

    static final String DEFAULT_TENANT_PROPERTY = "quarkus.multi-tenant.http.default-tenant";

    @Inject
    HttpTenantConfig config;

    @Inject
    TenantIdValidator validator;

    void onStart(@Observes StartupEvent event) {
        if (!config.enabled()) {
            return;
        }
        Optional<String> rejection = validator.validate(config.defaultTenant());
        if (rejection.isEmpty()) {
            return;
        }

        throw new IllegalStateException(
                "The configured default tenant (" + DEFAULT_TENANT_PROPERTY + ") violates the tenant-id policy: "
                        + rejection.get() + ". Set " + DEFAULT_TENANT_PROPERTY
                        + " to a non-reserved value that satisfies quarkus.multi-tenant.http.tenant-id.pattern and "
                        + "quarkus.multi-tenant.http.tenant-id.max-length. Length and pattern validation may be disabled "
                        + "with quarkus.multi-tenant.http.tenant-id.validation-enabled=false, but reserved identifiers "
                        + "are always rejected.");
    }
}
