/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package io.quarkiverse.multitenancy.orm.runtime.filter;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkiverse.multitenancy.core.runtime.validation.TenantIdValidator;
import io.quarkiverse.multitenancy.orm.runtime.config.OrmTenantConfig;
import io.quarkiverse.multitenancy.orm.runtime.validation.OrmTenantIdValidator;

@Provider
@Priority(Priorities.AUTHENTICATION + 100)
@ApplicationScoped
public class OrmTenantHeaderFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(OrmTenantHeaderFilter.class);

    @Inject
    TenantContext tenantContext;

    @Inject
    OrmTenantConfig config;

    @Inject
    OrmTenantIdValidator tenantIdValidator;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!config.headerFilter().enabled()) {
            return;
        }

        if (tenantContext.getTenantId().isPresent()) {
            logger.trace("Tenant already resolved; skipping ORM header fallback");
            return;
        }

        String tenant = requestContext.getHeaderString(config.headerFilter().headerName());
        if (tenant == null || tenant.isBlank()) {
            throw new WebApplicationException("Missing tenant header", 400);
        }

        Optional<String> rejection = tenantIdValidator.validate(tenant);
        if (rejection.isPresent()) {
            logger.debugf("Rejecting ORM fallback tenant id: %s", rejection.get());
            throw new WebApplicationException("Invalid tenant id", 400);
        }

        tenantContext.setTenantId(tenant);
        logger.debugf("Tenant resolved by ORM header fallback: '%s'",
                TenantIdValidator.sanitizeForLog(tenant));
    }
}
