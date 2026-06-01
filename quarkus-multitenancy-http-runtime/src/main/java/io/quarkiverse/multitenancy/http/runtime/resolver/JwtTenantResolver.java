package io.quarkiverse.multitenancy.http.runtime.resolver;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;

@ApplicationScoped
public class JwtTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(JwtTenantResolver.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    HttpTenantConfig config;

    @Override
    public String name() {
        return HttpTenantStrategy.jwt.name();
    }

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context found");
            return Optional.empty();
        }

        String authHeader = reqOpt.get().getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Authorization header missing or invalid");
            return Optional.empty();
        }

        String claimName = config.jwtClaimName();

        try {
            String token = authHeader.substring("Bearer ".length());
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                logger.debug("Invalid JWT format (missing payload)");
                return Optional.empty();
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = mapper.readTree(payloadJson);

            if (!payload.has(claimName)) {
                logger.debugf("JWT missing claim '%s'", claimName);
                return Optional.empty();
            }

            String tenant = payload.get(claimName).asText();
            logger.infof("Tenant claim '%s' decoded = '%s'", claimName, tenant);
            return Optional.of(tenant);

        } catch (Exception e) {
            logger.warnf("Failed to decode JWT: %s", e.getMessage());
            return Optional.empty();
        }
    }
}
