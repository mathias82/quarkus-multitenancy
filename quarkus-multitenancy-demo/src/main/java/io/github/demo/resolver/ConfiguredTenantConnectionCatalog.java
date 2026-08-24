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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ConfiguredTenantConnectionCatalog implements TenantConnectionCatalog {

    private static final Logger LOG = Logger.getLogger(ConfiguredTenantConnectionCatalog.class);

    private final Map<String, TenantDatabase> tenants;

    public ConfiguredTenantConnectionCatalog(TenantCatalogConfig config) {
        this.tenants = config.tenants().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> toDatabase(entry.getValue())));
        LOG.infov("Configured tenant connection catalog contains {0}", tenants.keySet());
    }

    @Override
    public Optional<TenantDatabase> find(String tenantId) {
        return Optional.ofNullable(tenants.get(tenantId));
    }

    @Override
    public Set<String> tenantIds() {
        return tenants.keySet();
    }

    private static TenantDatabase toDatabase(TenantCatalogConfig.Tenant tenant) {
        return new TenantDatabase(
                tenant.jdbcUrl(),
                tenant.username(),
                tenant.password(),
                tenant.initialSize(),
                tenant.minSize(),
                tenant.maxSize(),
                tenant.acquisitionTimeout(),
                tenant.maxLifetime());
    }
}
