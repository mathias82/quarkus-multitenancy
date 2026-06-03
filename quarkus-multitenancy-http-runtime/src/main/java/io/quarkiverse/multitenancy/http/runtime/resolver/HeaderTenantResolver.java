package io.quarkiverse.multitenancy.http.runtime.resolver;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;

@ApplicationScoped
public class HeaderTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(HeaderTenantResolver.class);

    @Inject
    HttpTenantConfig config;

    @Override
    public String name() {
        return HttpTenantStrategy.header.name();
    }

    @Override
    public TenantResolution resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context found");
            return TenantResolution.notApplicable();
        }

        String headerName = config.headerName();
        String header = reqOpt.get().getHeaderString(headerName);

        if (header == null || header.isBlank()) {
            logger.debugf("Header '%s' not found or empty", headerName);
            return TenantResolution.notApplicable();
        }

        String tenant = header.trim();
        logger.debugf("Resolved tenant '%s' from header '%s'", tenant, headerName);
        return TenantResolution.resolved(tenant);
    }
}
