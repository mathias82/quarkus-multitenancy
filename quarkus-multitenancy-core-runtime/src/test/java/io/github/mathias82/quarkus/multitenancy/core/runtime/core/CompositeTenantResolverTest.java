package io.github.mathias82.quarkus.multitenancy.core.runtime.core;

import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolutionContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.api.TenantResolver;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CompositeTenantResolverTest {

    @Inject
    CompositeTenantResolver compositeTenantResolver;

    @Test
    void shouldResolveTenantFromHeaderResolverFirst() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(String.class, "tenant-header");

        Optional<String> tenant = compositeTenantResolver.resolve(ctx);

        assertTrue(tenant.isPresent());
        assertEquals("tenant-header", tenant.get());
    }

    @Test
    void shouldResolveTenantFromCookieIfHeaderEmpty() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(Integer.class, 777);

        Optional<String> tenant = compositeTenantResolver.resolve(ctx);

        assertTrue(tenant.isPresent());
        assertEquals("777", tenant.get());
    }

    @Test
    void shouldReturnEmptyIfNoResolversMatch() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();

        Optional<String> tenant = compositeTenantResolver.resolve(ctx);

        assertTrue(tenant.isEmpty());
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
        public Optional<String> resolve(TenantResolutionContext context) {
            return context.get(String.class);
        }
    }

    @ApplicationScoped
    static class CookieTenantResolver implements TenantResolver {
        @Override
        public Optional<String> resolve(TenantResolutionContext context) {
            return context.get(Integer.class).map(Object::toString);
        }
    }
}
