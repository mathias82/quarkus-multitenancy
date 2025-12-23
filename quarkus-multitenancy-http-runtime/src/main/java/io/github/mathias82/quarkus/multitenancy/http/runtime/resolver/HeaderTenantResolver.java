package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class HeaderTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(HeaderTenantResolver.class);
    private static final String HEADER_NAME = "X-Tenant";

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context found");
            return Optional.empty();
        }

        ContainerRequestContext req = reqOpt.get();
        String header = req.getHeaderString(HEADER_NAME);

        if (header == null || header.isBlank()) {
            logger.debugf("Header '%s' not found or empty", HEADER_NAME);
            return Optional.empty();
        }

        String tenant = header.trim();
        logger.infof("Tenant header '%s' value resolved = '%s'", HEADER_NAME, tenant);
        return Optional.of(tenant);
    }
}
