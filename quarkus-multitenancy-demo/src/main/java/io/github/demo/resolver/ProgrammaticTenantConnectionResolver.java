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

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.github.demo.exception.TenantNotFoundException;
import io.quarkus.agroal.DataSource;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;

/**
 * Demonstrates Quarkus' existing programmatic tenant-connection SPI.
 * <p>
 * Providers are stable for the application lifetime. Update, rotation, drain,
 * and removal semantics intentionally remain outside this phase of the example.
 */
@ApplicationScoped
@PersistenceUnitExtension
public class ProgrammaticTenantConnectionResolver implements TenantConnectionResolver {

    private static final Logger LOG = Logger.getLogger(ProgrammaticTenantConnectionResolver.class);
    private static final String BOOTSTRAP_TENANT = "__bootstrap";

    private final TenantConnectionCatalog catalog;
    private final ConnectionProvider bootstrapProvider;
    private final NarayanaTransactionIntegration transactionIntegration;
    private final Map<String, ManagedProvider> providers = new ConcurrentHashMap<>();

    public ProgrammaticTenantConnectionResolver(TenantConnectionCatalog catalog,
            @DataSource(BOOTSTRAP_TENANT) AgroalDataSource bootstrapDataSource,
            TransactionManager transactionManager,
            TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.catalog = catalog;
        this.bootstrapProvider = new QuarkusConnectionProvider(bootstrapDataSource);
        this.transactionIntegration = new NarayanaTransactionIntegration(
                transactionManager, transactionSynchronizationRegistry);
    }

    @Override
    public ConnectionProvider resolve(String tenantId) {
        if (BOOTSTRAP_TENANT.equals(tenantId)) {
            return bootstrapProvider;
        }

        return providers.computeIfAbsent(tenantId, this::createProvider).connectionProvider();
    }

    int createdProviderCount() {
        return providers.size();
    }

    @PreDestroy
    void closeTenantPools() {
        providers.forEach((tenantId, provider) -> {
            LOG.debugv("Closing programmatic connection pool for tenant {0}", tenantId);
            provider.dataSource().close();
        });
        providers.clear();
    }

    private ManagedProvider createProvider(String tenantId) {
        TenantDatabase database = catalog.find(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        try {
            AgroalDataSource dataSource = AgroalDataSource.from(new AgroalDataSourceConfigurationSupplier()
                    .connectionPoolConfiguration(pool -> pool
                            .initialSize(database.initialSize())
                            .minSize(database.minSize())
                            .maxSize(database.maxSize())
                            .acquisitionTimeout(database.acquisitionTimeout())
                            .maxLifetime(database.maxLifetime())
                            .transactionIntegration(transactionIntegration)
                            .connectionFactoryConfiguration(connection -> connection
                                    .jdbcUrl(database.jdbcUrl())
                                    .principal(new NamePrincipal(database.username()))
                                    .credential(new SimplePassword(database.password())))));

            LOG.infov("Created programmatic connection pool for tenant {0}", tenantId);
            return new ManagedProvider(new QuarkusConnectionProvider(dataSource), dataSource);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create connection pool for tenant " + tenantId, e);
        }
    }

    private record ManagedProvider(ConnectionProvider connectionProvider, AgroalDataSource dataSource) {
    }
}
