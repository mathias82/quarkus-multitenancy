package io.quarkiverse.multitenancy.http.runtime.filter;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;
import io.quarkiverse.multitenancy.http.runtime.ctx.HttpTenantResolutionContext;

/**
 * HTTP tenant filter that resolves the current tenant using a strategy chain
 * driven by {@link HttpTenantConfig#strategy()}.
 *
 * <p>
 * Dispatch order:
 * <ol>
 * <li>User-defined (custom) {@link TenantResolver} beans — anything whose
 * {@link TenantResolver#name()} is blank or does not match a known
 * {@link HttpTenantStrategy}. Custom resolvers express explicit user
 * intent and run first.</li>
 * <li>Built-in resolvers in the order declared by
 * {@link HttpTenantConfig#strategy()}.</li>
 * </ol>
 *
 * <p>
 * If no resolver returns a value, the filter falls back to
 * {@link HttpTenantConfig#defaultTenant()}.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class TenantFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(TenantFilter.class);

    private static final Set<String> BUILT_IN_NAMES = EnumSet.allOf(HttpTenantStrategy.class).stream()
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    @Inject
    Instance<TenantResolver> resolvers;

    @Inject
    TenantContext tenantContext;

    @Inject
    HttpTenantConfig config;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (!config.enabled()) {
            return;
        }

        logger.debugf("Incoming request: %s %s",
                requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());

        HttpTenantResolutionContext ctx = new HttpTenantResolutionContext(requestContext);

        Optional<String> tenant = runCustomResolvers(ctx);

        if (tenant.isEmpty()) {
            tenant = runConfiguredBuiltins(ctx);
        }

        String resolved = tenant.orElseGet(config::defaultTenant);
        tenantContext.setTenantId(resolved);

        if (tenant.isPresent()) {
            logger.infof("Tenant successfully resolved: '%s'", resolved);
        } else {
            logger.debugf("No tenant resolved, falling back to default: '%s'", resolved);
        }
    }

    private Optional<String> runCustomResolvers(HttpTenantResolutionContext ctx) {
        for (TenantResolver r : resolvers) {
            if (isCustom(r)) {
                Optional<String> result = r.resolve(ctx);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> runConfiguredBuiltins(HttpTenantResolutionContext ctx) {
        for (String configured : config.strategy()) {
            HttpTenantStrategy strategy = parseStrategy(configured);
            if (strategy == null) {
                continue;
            }
            Optional<TenantResolver> match = findBuiltin(strategy);
            if (match.isPresent()) {
                Optional<String> result = match.get().resolve(ctx);
                if (result.isPresent()) {
                    return result;
                }
            } else {
                logger.debugf("No built-in resolver registered for strategy '%s'", strategy);
            }
        }
        return Optional.empty();
    }

    private HttpTenantStrategy parseStrategy(String configured) {
        if (configured == null || configured.isBlank()) {
            return null;
        }
        // Normalise to lowercase so values like "Header" or "JWT" in
        // quarkus.multi-tenant.http.strategy are accepted, matching the
        // case-insensitive behaviour users get from other Quarkus config.
        String normalized = configured.trim().toLowerCase(Locale.ROOT);
        try {
            return HttpTenantStrategy.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            logger.warnf("Unknown tenant strategy '%s' in quarkus.multi-tenant.http.strategy "
                    + "(expected one of %s); skipping.", configured, BUILT_IN_NAMES);
            return null;
        }
    }

    private Optional<TenantResolver> findBuiltin(HttpTenantStrategy strategy) {
        String target = strategy.name();
        for (TenantResolver r : resolvers) {
            if (target.equals(r.name())) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private boolean isCustom(TenantResolver r) {
        String n = r.name();
        return n == null || n.isBlank() || !BUILT_IN_NAMES.contains(n);
    }
}
