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
package io.quarkiverse.multitenancy.http.runtime.resolver;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;

@Priority(TenantResolver.DEFAULT_PRIORITY)
@ApplicationScoped
public class HeaderTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(HeaderTenantResolver.class);

    @Inject
    HttpTenantConfig config;

    @Override
    public String name() {
        return HttpTenantStrategy.header.name();
    }

    @Override
    public TenantResolution resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context found");
            return TenantResolution.notApplicable();
        }

        String headerName = config.headerName();
        String header = reqOpt.get().getHeaderString(headerName);

        if (header == null || header.isBlank()) {
            logger.debugf("Header '%s' not found or empty", headerName);
            return TenantResolution.notApplicable();
        }

        String tenant = header.trim();
        logger.debugf("Tenant resolved from header '%s'", headerName);
        return TenantResolution.resolved(tenant);
    }
}
