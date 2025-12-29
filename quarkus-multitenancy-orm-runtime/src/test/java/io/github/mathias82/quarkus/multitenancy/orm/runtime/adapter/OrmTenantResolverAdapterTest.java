package io.github.mathias82.quarkus.multitenancy.orm.runtime.adapter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class OrmTenantResolverAdapterTest {

    @Inject
    @PersistenceUnitExtension
    OrmTenantResolverAdapter resolver;

    @Inject
    TenantContext tenantContext;

    @Test
    void resolvesTenantFromTenantContext() {
        tenantContext.setTenantId("tenant2");

        String tenant = resolver.resolveTenantId();

        assertEquals("tenant2", tenant);
    }

    @Test
    void throwsExceptionWhenTenantNotSet() {
        tenantContext.clear();

        IllegalStateException ex =
                assertThrows(IllegalStateException.class,
                        resolver::resolveTenantId);

        assertEquals("Tenant not set before ORM access", ex.getMessage());
    }

    @Test
    void returnsBootstrapTenantWhenNoContextAvailable() {
        tenantContext.clear();

        String tenant = resolver.getDefaultTenantId();

        assertEquals(
                OrmTenantResolverAdapter.BOOTSTRAP_TENANT,
                tenant
        );
    }
}

