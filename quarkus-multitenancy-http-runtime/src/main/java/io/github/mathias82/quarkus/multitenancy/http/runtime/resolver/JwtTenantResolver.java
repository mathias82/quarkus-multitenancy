package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@ApplicationScoped
public class JwtTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(JwtTenantResolver.class);
    private static final String CLAIM_NAME = "tenant";
    private static final ObjectMapper mapper = new ObjectMapper();

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

        try {
            String token = authHeader.substring("Bearer ".length());
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                logger.debug("Invalid JWT format (missing payload)");
                return Optional.empty();
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = mapper.readTree(payloadJson);

            if (!payload.has(CLAIM_NAME)) {
                logger.debugf("JWT missing claim '%s'", CLAIM_NAME);
                return Optional.empty();
            }

            String tenant = payload.get(CLAIM_NAME).asText();
            logger.infof("Tenant claim decoded = '%s'", tenant);
            return Optional.of(tenant);

        } catch (Exception e) {
            logger.warnf("Failed to decode JWT: %s", e.getMessage());
            return Optional.empty();
        }
    }
}
