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
package io.github.demo.resolver;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import io.github.demo.exception.TenantNotFoundException;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;

@ApplicationScoped
public class SafeTenantResolver implements TenantResolver {

    private static final String HEADER_NAME = "X-Tenant";

    @Inject
    TenantDataSourceRegistry registry;

    @Override
    public TenantResolution resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);

        if (reqOpt.isEmpty()) {
            return TenantResolution.notApplicable();
        }

        ContainerRequestContext req = reqOpt.get();
        String tenantId = req.getHeaderString(HEADER_NAME);

        if (tenantId == null || tenantId.isBlank()) {
            return TenantResolution.notApplicable();
        }

        tenantId = tenantId.trim();

        if (!registry.exists(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }

        return TenantResolution.resolved(tenantId);
    }
}
