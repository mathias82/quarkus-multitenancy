package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.github.mathias82.quarkus.multitenancy.core.runtime.config.TenantStrategy;
import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;

import java.util.Map;
import java.util.Optional;

@TenantStrategy("cookie")
@ApplicationScoped
@Priority(400)
public class CookieTenantResolver implements TenantResolver {

    private final HttpTenantConfig config;

    @Inject
    public CookieTenantResolver(HttpTenantConfig config) {
        this.config = config;
    }

    @Override
    public Optional<String> resolve(TenantResolutionContext context) {

        Optional<ContainerRequestContext> reqOpt =
                context.get(ContainerRequestContext.class);

        if (reqOpt.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Cookie> cookies = reqOpt.get().getCookies();
        Cookie cookie = cookies.get(config.cookieName());

        if (cookie == null) {
            return Optional.empty();
        }

        String tenant = cookie.getValue();
        if (tenant == null || tenant.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(tenant);
    }
}

