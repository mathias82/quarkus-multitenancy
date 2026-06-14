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

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

/**
 * Shared JAX-RS resource for {@link OrmTenantHeaderFilter} integration tests.
 *
 * <p>
 * Echoes the value currently held by the request-scoped {@link TenantContext},
 * which lets the filter integration tests observe whether the filter populated
 * the context for the current request.
 */
@Path("/echo")
public class EchoResource {

    @Inject
    TenantContext tenantContext;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String echo() {
        return "tenant=" + tenantContext.getTenantId().orElse("(none)");
    }
}
