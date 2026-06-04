package io.quarkiverse.multitenancy.http.runtime.resolver;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.jboss.logging.Logger;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantStrategy;

/**
 * Resolves the tenant identifier from the request path using a configurable
 * regular expression. Default pattern matches {@code /t/{tenant}} or
 * {@code /t/{tenant}/...} and extracts the tenant from capturing group 1.
 */
@ApplicationScoped
public class PathTenantResolver implements TenantResolver {

    private static final Logger logger = Logger.getLogger(PathTenantResolver.class);

    @Inject
    HttpTenantConfig config;

    private Pattern pattern;
    private int group;

    @PostConstruct
    void init() {
        String configured = config.pathPattern();
        try {
            this.pattern = Pattern.compile(configured);
        } catch (PatternSyntaxException e) {
            logger.errorf("Invalid quarkus.multi-tenant.http.path-pattern '%s': %s",
                    configured, e.getDescription());
            throw e;
        }
        this.group = config.pathGroup();
        int patternGroupCount = pattern.matcher("").groupCount();
        if (group < 1 || group > patternGroupCount) {
            throw new IllegalArgumentException(String.format(
                    "Invalid quarkus.multi-tenant.http.path-group: %d. " +
                            "Must be between 1 and %d (number of capturing groups in pattern '%s'). " +
                            "Group 0 would match the entire regex instead of the tenant capture group.",
                    group, patternGroupCount, pattern.pattern()));
        }
    }

    @Override
    public String name() {
        return HttpTenantStrategy.path.name();
    }

    @Override
    public TenantResolution resolve(TenantResolutionContext context) {
        Optional<ContainerRequestContext> reqOpt = context.get(ContainerRequestContext.class);
        if (reqOpt.isEmpty()) {
            logger.debug("No request context found");
            return TenantResolution.notApplicable();
        }

        String path = reqOpt.get().getUriInfo().getPath();
        if (path == null || path.isBlank()) {
            return TenantResolution.notApplicable();
        }

        // UriInfo#getPath() drops the leading slash; restore it so users can
        // anchor their patterns naturally with '^/'.
        String normalized = path.startsWith("/") ? path : "/" + path;

        Matcher matcher = pattern.matcher(normalized);
        if (!matcher.find()) {
            logger.debugf("Path '%s' did not match pattern '%s'", normalized, pattern.pattern());
            return TenantResolution.notApplicable();
        }

        String tenant = matcher.group(group);
        if (tenant == null || tenant.isBlank()) {
            return TenantResolution.notApplicable();
        }

        String trimmed = tenant.trim();
        logger.debugf("Resolved tenant '%s' from path '%s'", trimmed, normalized);
        return TenantResolution.resolved(trimmed);
    }
}
