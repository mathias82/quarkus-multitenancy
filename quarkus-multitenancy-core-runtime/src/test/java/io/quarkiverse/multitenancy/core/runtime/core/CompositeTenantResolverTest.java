package io.quarkiverse.multitenancy.core.runtime.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CompositeTenantResolverTest {

    @Inject
    CompositeTenantResolver compositeTenantResolver;

    @Test
    void shouldResolveTenantFromHeaderResolverFirst() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(String.class, "tenant-header");

        TenantResolution result = compositeTenantResolver.resolve(ctx);

        TenantResolution.Resolved resolved = assertInstanceOf(TenantResolution.Resolved.class, result);
        assertEquals("tenant-header", resolved.tenantId());
    }

    @Test
    void shouldResolveTenantFromCookieIfHeaderEmpty() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(Integer.class, 777);

        TenantResolution result = compositeTenantResolver.resolve(ctx);

        TenantResolution.Resolved resolved = assertInstanceOf(TenantResolution.Resolved.class, result);
        assertEquals("777", resolved.tenantId());
    }

    @Test
    void shouldReturnNotApplicableIfNoResolversMatch() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();

        TenantResolution result = compositeTenantResolver.resolve(ctx);

        assertTrue(result instanceof TenantResolution.NotApplicable);
    }

    static class TestTenantResolutionContext implements TenantResolutionContext {
        private final Map<Class<?>, Object> values = new HashMap<>();

        public <T> void put(Class<T> type, T value) {
            values.put(type, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(Class<T> type) {
            return Optional.ofNullable((T) values.get(type));
        }
    }

    @ApplicationScoped
    static class HeaderTenantResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(String.class)
                    .<TenantResolution> map(TenantResolution::resolved)
                    .orElseGet(TenantResolution::notApplicable);
        }
    }

    @ApplicationScoped
    static class CookieTenantResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(Integer.class)
                    .<TenantResolution> map(i -> TenantResolution.resolved(i.toString()))
                    .orElseGet(TenantResolution::notApplicable);
        }
    }
}
