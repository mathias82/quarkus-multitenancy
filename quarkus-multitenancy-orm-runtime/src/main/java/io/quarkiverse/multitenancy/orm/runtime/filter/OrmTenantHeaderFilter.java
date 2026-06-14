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
package io.quarkiverse.multitenancy.orm.runtime.filter;

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
import io.quarkiverse.multitenancy.orm.runtime.config.OrmTenantConfig;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class OrmTenantHeaderFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(OrmTenantHeaderFilter.class);
    private static final String TENANT_HEADER = "X-Tenant";

    @Inject
    TenantContext tenantContext;

    @Inject
    OrmTenantConfig config;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (!config.headerFilter().enabled()) {
            return;
        }

        String tenant = requestContext.getHeaderString(TENANT_HEADER);

        if (tenant == null || tenant.isBlank()) {
            throw new WebApplicationException("Missing X-Tenant header", 400);
        }

        tenantContext.setTenantId(tenant);
        logger.infof("Tenant resolved from header: '%s'", tenant);
    }
}
