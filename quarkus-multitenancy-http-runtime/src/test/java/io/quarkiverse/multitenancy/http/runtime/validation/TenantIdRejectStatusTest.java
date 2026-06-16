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
package io.quarkiverse.multitenancy.http.runtime.validation;

import static io.restassured.RestAssured.given;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * Verifies {@code quarkus.multi-tenant.http.tenant-id.reject-status}: a custom
 * status is used for <em>syntactic</em> tenant-id validation failures, while an
 * authentication-related {@code TenantResolution.Rejected} keeps returning 401
 * regardless of the configured reject status.
 */
@QuarkusTest
@TestProfile(TenantIdRejectStatusTest.CustomRejectStatusProfile.class)
class TenantIdRejectStatusTest {

    @Test
    void usesConfiguredStatusForOverlongTenantId() {
        given()
                .header("X-Tenant", "0123456789abcdefg") // 17 chars, max is 16
                .when()
                .get("/tenant")
                .then()
                .statusCode(422);
    }

    @Test
    void usesConfiguredStatusForDisallowedCharacters() {
        given()
                .header("X-Tenant", "acme;DROP") // ';' is outside [A-Za-z0-9_-]
                .when()
                .get("/tenant")
                .then()
                .statusCode(422);
    }

    @Test
    void authenticationRejectionStillReturns401() {
        // A Rejected outcome (here: a custom resolver locking down a header) is
        // an authentication-style failure and must stay 401 — the configurable
        // reject status only governs syntactic tenant-id validation.
        given()
                .header("X-Bad-Tenant", "anything")
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    /**
     * Sets a non-default reject status (422) and caps the length at 16. A custom
     * resolver provides the authentication-style rejection path.
     */
    public static class CustomRejectStatusProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.multi-tenant.http.strategy", "header",
                    "quarkus.multi-tenant.http.tenant-id.max-length", "16",
                    "quarkus.multi-tenant.http.tenant-id.reject-status", "422");
        }
    }

    /**
     * Rejects when {@code X-Bad-Tenant} is present, otherwise stays out of the
     * way so the built-in header resolver runs.
     */
    @ApplicationScoped
    public static class RejectingResolver implements TenantResolver {

        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(jakarta.ws.rs.container.ContainerRequestContext.class)
                    .map(req -> req.getHeaderString("X-Bad-Tenant") != null
                            ? TenantResolution.rejected("X-Bad-Tenant header is not allowed")
                            : TenantResolution.notApplicable())
                    .orElseGet(TenantResolution::notApplicable);
        }
    }
}
