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
package io.quarkiverse.multitenancy.core.runtime.api;

/**
 * Outcome of a {@link TenantResolver#resolve(TenantResolutionContext)} call.
 *
 * <p>
 * A resolver can return one of three outcomes, which the dispatcher (e.g.
 * {@code TenantFilter}) uses to decide what to do next:
 *
 * <ul>
 * <li>{@link Resolved} — the resolver successfully resolved a tenant id. The
 * dispatcher sets the tenant context to this id and stops the strategy
 * chain.</li>
 * <li>{@link NotApplicable} — the resolver had nothing to act on (for
 * example, no JWT token was present). The dispatcher continues with the
 * next strategy and ultimately falls back to the configured default
 * tenant.</li>
 * <li>{@link Rejected} — the resolver did act on the input but found it
 * invalid, malformed, unverifiable, or missing the required tenant claim.
 * The dispatcher must abort the request with 401 and must never fall back
 * to the default tenant.</li>
 * </ul>
 *
 * <p>
 * This split makes the security behaviour explicit: a present-but-invalid
 * token cannot silently downgrade to the default tenant.
 */
public sealed interface TenantResolution
        permits TenantResolution.Resolved,
        TenantResolution.NotApplicable,
        TenantResolution.Rejected {

    /**
     * Resolved outcome carrying the successfully resolved tenant id.
     *
     * @param tenantId the resolved tenant id; must not be {@code null} or blank
     * @return a {@link Resolved} outcome
     */
    static TenantResolution resolved(String tenantId) {
        return new Resolved(tenantId);
    }

    /**
     * Indicates that this resolver had nothing applicable to process and the
     * dispatcher should continue with the next strategy.
     *
     * @return a {@link NotApplicable} outcome
     */
    static TenantResolution notApplicable() {
        return new NotApplicable();
    }

    /**
     * Indicates that this resolver processed the input and found it invalid.
     * The dispatcher must abort the request with 401 and must not fall back
     * to the default tenant.
     *
     * @param reason a human-readable reason; defaults to "Tenant resolution rejected"
     *        when null or blank
     * @return a {@link Rejected} outcome
     */
    static TenantResolution rejected(String reason) {
        return new Rejected(reason);
    }

    /**
     * The resolver successfully produced a tenant id.
     */
    record Resolved(String tenantId) implements TenantResolution {
        public Resolved {
            if (tenantId == null || tenantId.isBlank()) {
                throw new IllegalArgumentException("tenantId must not be blank");
            }
        }
    }

    /**
     * The resolver had no applicable input; the dispatcher should try the next
     * strategy.
     */
    record NotApplicable() implements TenantResolution {
    }

    /**
     * The resolver acted on the input and decided to reject the request. The
     * dispatcher must respond with 401 and must not fall back to a default
     * tenant.
     */
    record Rejected(String reason) implements TenantResolution {
        public Rejected {
            if (reason == null || reason.isBlank()) {
                reason = "Tenant resolution rejected";
            }
        }
    }
}
