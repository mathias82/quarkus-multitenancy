package io.github.mathias82.quarkus.multitenancy.orm.runtime.adapter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrmTenantResolverAdapterTest {

    @Test
    void resolvesTenantFromTenantContext() {
        OrmTenantResolverAdapter adapter = new OrmTenantResolverAdapter();

        TestTenantContext ctx = new TestTenantContext();
        ctx.setTenantId("tenant2");

        adapter.tenantContext = ctx;

        String tenant = adapter.resolveTenantId();

        assertEquals("tenant2", tenant);
    }

    @Test
    void throwsExceptionWhenTenantNotSet() {
        OrmTenantResolverAdapter adapter = new OrmTenantResolverAdapter();
        adapter.tenantContext = new TestTenantContext();

        IllegalStateException ex =
                assertThrows(IllegalStateException.class,
                        adapter::resolveTenantId);

        assertEquals(
                "Tenant not set before ORM access",
                ex.getMessage()
        );
    }


    static class TestTenantContext implements TenantContext {

        private String tenant;

        @Override
        public Optional<String> getTenantId() {
            return Optional.ofNullable(tenant);
        }

        @Override
        public void setTenantId(String tenantId) {
            this.tenant = tenantId;
        }

        @Override
        public void clear() {
            this.tenant = null;
        }
    }
}
