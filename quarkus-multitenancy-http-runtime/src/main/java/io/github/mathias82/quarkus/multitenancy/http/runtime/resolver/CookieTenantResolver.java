package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class CookieTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(CookieTenantResolver.class);

    @Inject
    HttpTenantConfig config;

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.info("No request context found");
            return Optional.empty();
        }

        ContainerRequestContext req = reqOpt.get();
        String cookieName = config.cookieName();

        if (req.getCookies() == null || req.getCookies().isEmpty()) {
            logger.info("No cookies found in request");
            return Optional.empty();
        }

        var cookie = req.getCookies().get(cookieName);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            logger.debugf("Cookie '%s' not found or empty", cookieName);
            return Optional.empty();
        }

        String tenant = cookie.getValue().trim();
        logger.infof("Tenant cookie value resolved = '%s'", tenant);
        return Optional.of(tenant);
    }
}
