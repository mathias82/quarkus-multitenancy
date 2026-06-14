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
package io.quarkiverse.multitenancy.core.runtime.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;

/**
 * Runs every available {@link TenantResolver} in turn and surfaces the first
 * non-{@link TenantResolution.NotApplicable} outcome. A {@link TenantResolution.Rejected}
 * short-circuits the chain — callers are expected to treat that as an
 * unrecoverable failure for the current request and must not fall back to a
 * default tenant.
 */
@ApplicationScoped
public class CompositeTenantResolver {

    private static final Logger logger = Logger.getLogger(CompositeTenantResolver.class);

    @Inject
    Instance<TenantResolver> resolvers;

    public TenantResolution resolve(TenantResolutionContext context) {
        if (resolvers.isUnsatisfied()) {
            logger.debug("No TenantResolvers found");
            return TenantResolution.notApplicable();
        }

        for (TenantResolver resolver : resolvers) {
            TenantResolution result = resolver.resolve(context);
            if (result instanceof TenantResolution.Resolved resolved) {
                logger.debugf("Tenant resolved by %s = '%s'",
                        resolver.getClass().getSimpleName(), resolved.tenantId());
                return result;
            }
            if (result instanceof TenantResolution.Rejected rejected) {
                logger.debugf("Tenant resolution rejected by %s: %s",
                        resolver.getClass().getSimpleName(), rejected.reason());
                return result;
            }
        }

        logger.debug("No resolver produced a tenant");
        return TenantResolution.notApplicable();
    }
}
