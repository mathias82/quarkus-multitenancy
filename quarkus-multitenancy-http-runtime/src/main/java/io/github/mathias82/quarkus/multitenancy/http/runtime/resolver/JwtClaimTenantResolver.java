package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.TenantStrategy;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@TenantStrategy("jwt")
@ApplicationScoped
@Priority(200)
public class JwtClaimTenantResolver implements TenantResolver {

    private final HttpTenantConfig config;

    @Inject
    public JwtClaimTenantResolver(HttpTenantConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {

        Optional<ContainerRequestContext> reqOpt =
                context.get(ContainerRequestContext.class);

        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }

        String auth = reqOpt.get()
                .getHeaderString(HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = auth.substring("Bearer ".length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return Optional.empty();
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            JsonObject payload =
                    new JsonObject(new String(decoded, StandardCharsets.UTF_8));

            String tenant = payload.getString(config.jwtClaimName());
            if (tenant == null || tenant.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(tenant);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
