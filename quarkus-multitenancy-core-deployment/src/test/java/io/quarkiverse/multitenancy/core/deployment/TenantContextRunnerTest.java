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
package io.quarkiverse.multitenancy.core.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.quarkiverse.multitenancy.core.runtime.context.TenantContextRunner;
import io.quarkus.arc.Arc;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

@QuarkusTest
class TenantContextRunnerTest {

    @Inject
    TenantContext tenantContext;

    @Inject
    TenantContextRunner runner;

    @Test
    @ActivateRequestContext
    void shouldRestoreTenantAfterNestedAndSequentialCalls() {
        tenantContext.setTenantId("original");

        runner.runAsTenant("tenant-a", () -> {
            assertEquals("tenant-a", tenantContext.getTenantId().orElseThrow());

            String nestedTenant = runner.runAsTenant("tenant-b", () -> tenantContext.getTenantId().orElseThrow());
            assertEquals("tenant-b", nestedTenant);
            assertEquals("tenant-a", tenantContext.getTenantId().orElseThrow());
        });

        assertEquals("original", tenantContext.getTenantId().orElseThrow());

        runner.runAsTenant("tenant-c", () -> assertEquals("tenant-c", tenantContext.getTenantId().orElseThrow()));
        assertEquals("original", tenantContext.getTenantId().orElseThrow());
    }

    @Test
    @ActivateRequestContext
    void shouldRestoreTenantWhenWorkFails() {
        tenantContext.setTenantId("original");

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> runner.runAsTenant("tenant-a", () -> {
                    assertEquals("tenant-a", tenantContext.getTenantId().orElseThrow());
                    throw new IllegalStateException("boom");
                }));

        assertEquals("boom", failure.getMessage());
        assertEquals("original", tenantContext.getTenantId().orElseThrow());
    }

    @Test
    @ActivateRequestContext
    void shouldClearTenantWhenNoneWasPreviouslyBound() {
        tenantContext.clear();

        runner.runAsTenant("tenant-a", () -> assertEquals("tenant-a", tenantContext.getTenantId().orElseThrow()));

        assertTrue(tenantContext.getTenantId().isEmpty());
    }

    @Test
    @ActivateRequestContext
    void shouldRejectCompletionStageAndRestorePreviousTenant() {
        tenantContext.setTenantId("original");

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> runner.runAsTenant("tenant-a", () -> CompletableFuture.completedFuture("done")));

        assertTrue(failure.getMessage().contains("synchronous work"));
        assertEquals("original", tenantContext.getTenantId().orElseThrow());
    }

    @Test
    @ActivateRequestContext
    void shouldRejectUniAndRestorePreviousTenant() {
        tenantContext.setTenantId("original");

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> runner.runAsTenant("tenant-a", () -> Uni.createFrom().item("done")));

        assertTrue(failure.getMessage().contains("Uni or CompletionStage"));
        assertEquals("original", tenantContext.getTenantId().orElseThrow());
    }

    @Test
    @ActivateRequestContext
    void shouldRejectDeferredUniBeforeItsBodyRuns() {
        // A deferred Uni whose item supplier flips the flag only when subscribed. The runner must
        // reject the Uni without subscribing, so the deferred body never runs after the tenant
        // context has already been restored — the exact silent-async footgun issue #66 guards.
        AtomicBoolean deferredBodyRan = new AtomicBoolean(false);

        assertThrows(IllegalStateException.class,
                () -> runner.runAsTenant("tenant-a",
                        () -> Uni.createFrom().item(() -> {
                            deferredBodyRan.set(true);
                            return "done";
                        })));

        assertFalse(deferredBodyRan.get());
    }

    @Test
    void shouldRejectCompletionStageAndCloseOwnedRequestContext() throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertFalse(Arc.container().requestContext().isActive());

                IllegalStateException failure = assertThrows(IllegalStateException.class,
                        () -> runner.runAsTenant("tenant-a", () -> CompletableFuture.completedFuture("done")));

                assertTrue(failure.getMessage().contains("Uni or CompletionStage"));
                assertFalse(Arc.container().requestContext().isActive());
            }).get();
        } catch (ExecutionException e) {
            throw new AssertionError(e.getCause());
        }
    }

    @Test
    void shouldOwnRequestContextWhenNoneIsActive() throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertFalse(Arc.container().requestContext().isActive());

                String tenant = runner.runAsTenant("tenant-a", () -> {
                    assertTrue(Arc.container().requestContext().isActive());
                    return tenantContext.getTenantId().orElseThrow();
                });

                assertEquals("tenant-a", tenant);
                assertFalse(Arc.container().requestContext().isActive());

                assertThrows(IllegalStateException.class,
                        () -> runner.runAsTenant("tenant-b", () -> {
                            throw new IllegalStateException("boom");
                        }));
                assertFalse(Arc.container().requestContext().isActive());
            }).get();
        } catch (ExecutionException e) {
            throw new AssertionError(e.getCause());
        }
    }
}
