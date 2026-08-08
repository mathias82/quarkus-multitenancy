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
package io.quarkiverse.multitenancy.orm.runtime.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.orm.runtime.config.OrmTenantConfig;

/**
 * ORM-configured adapter for the shared core tenant-id validator.
 */
@ApplicationScoped
public class OrmTenantIdValidator
        extends io.quarkiverse.multitenancy.core.runtime.validation.TenantIdValidator {

    @Inject
    public OrmTenantIdValidator(OrmTenantConfig config) {
        super(config.tenantId().validationEnabled(),
                config.tenantId().maxLength(),
                config.tenantId().pattern(),
                "quarkus.multi-tenant.orm.tenant-id.pattern");
    }
}
