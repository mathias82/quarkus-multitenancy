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
package io.quarkiverse.multitenancy.http.runtime.resolver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * Verifies that {@code quarkus.multi-tenant.http.header-name} is honored
 * by {@link HeaderTenantResolver}. Before this PR the resolver hardcoded
 * {@code X-Tenant} and overrides in {@code application.properties} were
 * silently ignored.
 */
@QuarkusTest
@TestProfile(HeaderTenantResolverConfigOverrideTest.CustomHeaderProfile.class)
class HeaderTenantResolverConfigOverrideTest {

    @Test
    void resolvesTenantFromOverriddenHeaderName() {
        given()
                .header("X-Org-Id", "tenant-from-override")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-from-override]"));
    }

    @Test
    void ignoresDefaultHeaderWhenOverridden() {
        given()
                .header("X-Tenant", "should-be-ignored")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[public]"));
    }

    public static class CustomHeaderProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.smallrye-jwt.enabled", "false",
                    "quarkus.multi-tenant.http.strategy", "header",
                    "quarkus.multi-tenant.http.header-name", "X-Org-Id");
        }
    }
}
