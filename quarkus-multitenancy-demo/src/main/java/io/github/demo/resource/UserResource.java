package io.github.demo.resource;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
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
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
}
