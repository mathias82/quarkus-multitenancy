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
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Performs real Hibernate ORM access so the integration suite can verify tenant-specific
 * datasource routing rather than only observing the resolved tenant id.
 */
@Path("/orm")
@Produces(MediaType.TEXT_PLAIN)
public class OrmTenantResource {

    private static final long PROBE_ID = 1L;

    @Inject
    EntityManager entityManager;

    @POST
    @Path("/{label}")
    @Transactional
    public Response store(@PathParam("label") String label) {
        entityManager.merge(new TenantProbe(PROBE_ID, label));
        return Response.noContent().build();
    }

    @GET
    @Transactional
    public String load() {
        TenantProbe probe = entityManager.find(TenantProbe.class, PROBE_ID);
        return probe == null ? "<empty>" : probe.getTenantLabel();
    }
}
