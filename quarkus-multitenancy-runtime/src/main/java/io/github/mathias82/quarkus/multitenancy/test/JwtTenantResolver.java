package io.github.mathias82.quarkus.multitenancy.test;

import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.runtime.config.MultiTenantConfig;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@ApplicationScoped
@Priority(200)
public class JwtTenantResolver implements TenantResolver {

    private final MultiTenantConfig config;

    @Inject
    public JwtTenantResolver(MultiTenantConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> resolve(TenantResolutionContext context) {

        Optional<Object> raw = context.get(MultivaluedMap.class);
        if (raw.isEmpty()) {
            return Optional.empty();
        }

        MultivaluedMap<String, String> headers =
                (MultivaluedMap<String, String>) raw.get();

        String auth = headers.getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = auth.substring("Bearer ".length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return Optional.empty();
        }

        try {
            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);

            JsonObject payload = new JsonObject(payloadJson);
            String tenant = payload.getString(config.jwtClaimName());

            if (tenant == null || tenant.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(tenant);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
