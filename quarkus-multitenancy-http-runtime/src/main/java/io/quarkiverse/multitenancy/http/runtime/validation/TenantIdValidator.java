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
package io.quarkiverse.multitenancy.http.runtime.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;

/**
 * HTTP-configured compatibility facade over the shared core tenant-id validator.
 *
 * <p>
 * Keeps the existing HTTP validator type available while delegating the validation
 * policy to the common core implementation used by other resolution paths.
 */
@ApplicationScoped
public class TenantIdValidator
        extends io.quarkiverse.multitenancy.core.runtime.validation.TenantIdValidator {

    /**
     * Creates the validator from the HTTP tenant-id configuration.
     *
     * @param config HTTP multitenancy configuration
     */
    @Inject
    public TenantIdValidator(HttpTenantConfig config) {
        super(config.tenantId().validationEnabled(),
                config.tenantId().maxLength(),
                config.tenantId().pattern(),
                "quarkus.multi-tenant.http.tenant-id.pattern");
    }
}
