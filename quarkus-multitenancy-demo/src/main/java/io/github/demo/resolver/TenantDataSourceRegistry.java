/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.demo.resolver;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

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
