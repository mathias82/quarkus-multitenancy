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
package io.quarkiverse.multitenancy.core.runtime.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;
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

    @Test
    void shouldRunHigherPriorityResolverFirst() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(String.class, "lower-priority");
        ctx.put(Integer.class, 777);

        TenantResolution result = compositeTenantResolver.resolve(ctx);

        TenantResolution.Resolved resolved = assertInstanceOf(TenantResolution.Resolved.class, result);
        assertEquals("777", resolved.tenantId());
    }

    @Test
    void shouldUseImplementationClassNameForEqualPriorities() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(Long.class, 1L);

        TenantResolution result = compositeTenantResolver.resolve(ctx);

        TenantResolution.Resolved resolved = assertInstanceOf(TenantResolution.Resolved.class, result);
        assertEquals("alpha-equal", resolved.tenantId());
    }

    @Test
    void shouldUseStableDefaultPriorityOrder() {
        TestTenantResolutionContext ctx = new TestTenantResolutionContext();
        ctx.put(Double.class, 1.0);

        TenantResolution result = compositeTenantResolver.resolve(ctx);

        TenantResolution.Resolved resolved = assertInstanceOf(TenantResolution.Resolved.class, result);
        assertEquals("alpha-default", resolved.tenantId());
    }

    @Test
    void shouldRejectIrreduciblyAmbiguousOrder() {
        String implementationClass = HeaderTenantResolver.class.getName();
        var descriptors = List.of(
                new TenantResolverRegistry.ResolverDescriptor(100, implementationClass, "first-bean"),
                new TenantResolverRegistry.ResolverDescriptor(100, implementationClass, "second-bean"));

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> TenantResolverRegistry.validateDeterministicTieBreakers(descriptors));

        assertTrue(failure.getMessage().contains("Ambiguous TenantResolver order"));
        assertTrue(failure.getMessage().contains("@Priority"));
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
    @Priority(100)
    static class HeaderTenantResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(String.class)
                    .<TenantResolution> map(TenantResolution::resolved)
                    .orElseGet(TenantResolution::notApplicable);
        }
    }

    @ApplicationScoped
    @Priority(200)
    static class CookieTenantResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(Integer.class)
                    .<TenantResolution> map(i -> TenantResolution.resolved(i.toString()))
                    .orElseGet(TenantResolution::notApplicable);
        }
    }

    @ApplicationScoped
    @Priority(50)
    static class AlphaEqualPriorityResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(Long.class)
                    .<TenantResolution> map(ignored -> TenantResolution.resolved("alpha-equal"))
                    .orElseGet(TenantResolution::notApplicable);
        }
    }

    @ApplicationScoped
    @Priority(50)
    static class ZuluEqualPriorityResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(Long.class)
                    .<TenantResolution> map(ignored -> TenantResolution.resolved("zulu-equal"))
                    .orElseGet(TenantResolution::notApplicable);
        }
    }

    @ApplicationScoped
    static class AlphaDefaultPriorityResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(Double.class)
                    .<TenantResolution> map(ignored -> TenantResolution.resolved("alpha-default"))
                    .orElseGet(TenantResolution::notApplicable);
        }
    }

    @ApplicationScoped
    static class ZuluDefaultPriorityResolver implements TenantResolver {
        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(Double.class)
                    .<TenantResolution> map(ignored -> TenantResolution.resolved("zulu-default"))
                    .orElseGet(TenantResolution::notApplicable);
        }
    }
}
