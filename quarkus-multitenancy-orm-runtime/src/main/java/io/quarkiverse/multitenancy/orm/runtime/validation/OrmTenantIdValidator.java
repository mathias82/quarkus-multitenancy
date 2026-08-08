/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package io.quarkiverse.multitenancy.orm.runtime.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.orm.runtime.config.OrmTenantConfig;

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
