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
package io.github.demo.resource;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.github.demo.entity.User;
import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    TenantContext tenantContext;

    @GET
    public List<User> listAll() {
        return User.listAll();
    }

    @POST
    @Transactional
    public User create(User user) {
        user.persist();
        return user;
    }

    @GET
    @Path("/tenant")
    public Optional<String> tenant() {
        return tenantContext.getTenantId();
    }

    @GET
    @Path("/tenant/path/{tenant}")
    public Optional<String> pathTenant(@PathParam("tenant") String ignored) {
        return tenantContext.getTenantId();
    }
}
