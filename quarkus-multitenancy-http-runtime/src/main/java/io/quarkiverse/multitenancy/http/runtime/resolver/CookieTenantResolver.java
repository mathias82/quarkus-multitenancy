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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;

@ApplicationScoped
public class CookieTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(CookieTenantResolver.class);

    @Inject
    HttpTenantConfig config;

    @Override
    public String name() {
        return HttpTenantStrategy.cookie.name();
    }

    @Override
    public TenantResolution resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context found");
            return TenantResolution.notApplicable();
        }

        ContainerRequestContext req = reqOpt.get();
        String cookieName = config.cookieName();

        if (req.getCookies() == null || req.getCookies().isEmpty()) {
            logger.debug("No cookies found in request");
            return TenantResolution.notApplicable();
        }

        var cookie = req.getCookies().get(cookieName);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            logger.debugf("Cookie '%s' not found or empty", cookieName);
            return TenantResolution.notApplicable();
        }

        String tenant = cookie.getValue().trim();
        logger.debugf("Resolved tenant '%s' from cookie '%s'", tenant, cookieName);
        return TenantResolution.resolved(tenant);
    }
}
