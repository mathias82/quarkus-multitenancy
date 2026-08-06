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
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;

/**
 * Runs work with a tenant temporarily bound to the current thread.
 * <p>
 * If the CDI request context is not active, this runner activates it for the
 * duration of the work. An existing tenant binding is always restored, which
 * makes nested and sequential invocations safe.
 */
@ApplicationScoped
public class TenantContextRunner {

    private final TenantContext tenantContext;

    @Inject
    public TenantContextRunner(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    /**
     * Runs work with the supplied tenant bound.
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
     * Runs work with the supplied tenant bound and returns its result.
     *
     * @param tenantId tenant to bind; must not be {@code null}
     * @param work work to run; must not be {@code null}
     * @param <T> result type
     * @return the result returned by the work
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
                return work.get();
            } finally {
                previousTenant.ifPresentOrElse(tenantContext::setTenantId, tenantContext::clear);
            }
        } finally {
            if (activated) {
                requestContext.terminate();
            }
        }
    }
}
