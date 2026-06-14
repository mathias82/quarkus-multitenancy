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
package io.quarkiverse.multitenancy.http.runtime.filter;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

/**
 * Test resource exposing endpoints under {@code /t/{tenant}/...} so the
 * {@code PathTenantResolver} has a request path that matches its default
 * pattern.
 */
@Path("/t/{tenant}/tenant")
public class PathTenantResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public Optional<String> tenant() {
        return tenantContext.getTenantId();
    }
}
