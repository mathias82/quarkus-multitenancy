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
package io.quarkiverse.multitenancy.http.runtime.startup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkus.runtime.StartupEvent;

/**
 * Fails fast at startup when {@code reject-status} is configured with a value
 * outside the {@code 4xx} range.
 *
 * <p>
 * A resolved tenant id that violates the {@code max-length} or {@code pattern}
 * policy is a client-side input problem, so the abort status produced by
 * {@code TenantFilter} must always be a client error. The property is a raw
 * integer passed to {@code Response.status(...)}, which only rejects values
 * below {@code 100}; misconfigurations such as {@code 200}, {@code 302} or
 * {@code 500} would otherwise be accepted silently and produce misleading
 * behaviour. Catching the mistake at boot turns a latent runtime surprise into
 * an immediate, actionable configuration error.
 *
 * <p>
 * The check only runs when it can actually matter: the extension must be
 * {@link HttpTenantConfig#enabled() enabled} and tenant-id
 * {@link HttpTenantConfig.TenantIdConfig#validationEnabled() validation} must be
 * on, because {@code reject-status} is consulted only on a validation
 * rejection. When validation is disabled the property is inert and is left
 * untouched.
 */
@ApplicationScoped
public class RejectStatusStartupValidator {

    private static final String REJECT_STATUS_PROPERTY = "quarkus.multi-tenant.http.tenant-id.reject-status";

    private static final int MIN_CLIENT_ERROR_STATUS = 400;

    private static final int MAX_CLIENT_ERROR_STATUS = 499;

    @Inject
    HttpTenantConfig config;

    void onStart(@Observes StartupEvent event) {
        if (!config.enabled()) {
            return;
        }
        if (!config.tenantId().validationEnabled()) {
            return;
        }

        int rejectStatus = config.tenantId().rejectStatus();
        if (rejectStatus < MIN_CLIENT_ERROR_STATUS || rejectStatus > MAX_CLIENT_ERROR_STATUS) {
            throw new IllegalStateException(
                    REJECT_STATUS_PROPERTY + " is " + rejectStatus
                            + ", but a tenant-id validation failure is a client error: only 4xx statuses in the range ["
                            + MIN_CLIENT_ERROR_STATUS + "," + MAX_CLIENT_ERROR_STATUS
                            + "] are supported. Set " + REJECT_STATUS_PROPERTY
                            + " to a value such as 400 (the default) or 401.");
        }
    }
}
