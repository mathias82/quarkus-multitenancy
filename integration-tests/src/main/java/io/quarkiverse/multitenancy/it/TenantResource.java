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
package io.quarkiverse.multitenancy.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

/**
 * Echoes the tenant resolved by the HTTP filter so the integration tests can assert the
 * resolution outcome. A request only reaches this resource once the filter has either
 * resolved a tenant (including the configured default) or has already rejected it, so the
 * {@link TenantContext} is always populated here.
 */
@Path("/tenant")
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String tenant() {
        return tenantContext.getTenantId().orElse("<none>");
    }
}
