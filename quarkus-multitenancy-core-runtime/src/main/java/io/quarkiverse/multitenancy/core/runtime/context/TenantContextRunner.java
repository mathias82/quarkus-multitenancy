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
package io.quarkiverse.multitenancy.core.runtime.context;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;

/**
 * Runs synchronous work with a tenant temporarily bound to the current thread.
 * <p>
 * If the CDI request context is not active, this runner activates it for the
 * duration of the work. An existing tenant binding is always restored, which
 * makes nested and sequential invocations safe.
 * <p>
 * This runner deliberately does not propagate the tenant across asynchronous
 * boundaries. Returning a {@link CompletionStage} or a Mutiny {@code Uni}
 * from the supplier is rejected so that accidental asynchronous use fails
 * immediately instead of running after the tenant context has been restored.
 */
@ApplicationScoped
public class TenantContextRunner {

    private static final String MUTINY_UNI_CLASS_NAME = "io.smallrye.mutiny.Uni";

    private final TenantContext tenantContext;

    @Inject
    public TenantContextRunner(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    /**
     * Runs synchronous work with the supplied tenant bound.
     *
     * @param tenantId tenant to bind; must not be {@code null}
     * @param work work to run; must not be {@code null}
     */
    public void runAsTenant(String tenantId, Runnable work) {
        requireNonNull(work, "work");
        runAsTenant(tenantId, () -> {
            work.run();
            return null;
        });
    }

    /**
     * Runs synchronous work with the supplied tenant bound and returns its result.
     * Asynchronous return values such as {@link CompletionStage} and Mutiny
     * {@code Uni} are not supported and are rejected immediately.
     *
     * @param tenantId tenant to bind; must not be {@code null}
     * @param work work to run; must not be {@code null}
     * @param <T> result type
     * @return the result returned by the work
     * @throws IllegalStateException when the supplier returns an asynchronous result
     */
    public <T> T runAsTenant(String tenantId, Supplier<T> work) {
        requireNonNull(tenantId, "tenantId");
        requireNonNull(work, "work");

        ManagedContext requestContext = Arc.container().requestContext();
        boolean activated = !requestContext.isActive();
        if (activated) {
            requestContext.activate();
        }

        try {
            Optional<String> previousTenant = tenantContext.getTenantId();
            tenantContext.setTenantId(tenantId);
            try {
                T result = work.get();
                rejectAsyncResult(result);
                return result;
            } finally {
                previousTenant.ifPresentOrElse(tenantContext::setTenantId, tenantContext::clear);
            }
        } finally {
            if (activated) {
                requestContext.terminate();
            }
        }
    }

    private static void rejectAsyncResult(Object result) {
        if (result == null) {
            return;
        }
        if (result instanceof CompletionStage<?> || isMutinyUni(result)) {
            throw new IllegalStateException(
                    "TenantContextRunner only supports synchronous work; do not return Uni or CompletionStage values");
        }
    }

    private static boolean isMutinyUni(Object result) {
        try {
            Class<?> uniType = Class.forName(MUTINY_UNI_CLASS_NAME, false, result.getClass().getClassLoader());
            return uniType.isInstance(result);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
