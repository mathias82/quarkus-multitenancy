package io.github.demo.resolver;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class TenantDataSourceRegistry {

    private static final Logger logger = Logger.getLogger(TenantDataSourceRegistry.class);
    private final Set<String> configuredTenants;

    public TenantDataSourceRegistry() {
        Config config = ConfigProvider.getConfig();

        this.configuredTenants = StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                .filter(p -> p.startsWith("quarkus.datasource.") && p.endsWith(".jdbc.url"))
                .map(p -> p.replace("quarkus.datasource.", "").replace(".jdbc.url", ""))
                .collect(Collectors.toSet());

        logger.info("Configured tenants detected:" + configuredTenants);
    }

    public boolean exists(String tenantId) {
        return configuredTenants.contains(tenantId);
    }

    public Set<String> getConfiguredTenants() {
        return configuredTenants;
    }
}
