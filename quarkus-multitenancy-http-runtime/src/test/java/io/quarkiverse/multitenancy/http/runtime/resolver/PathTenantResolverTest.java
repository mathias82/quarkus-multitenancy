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

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(PathTenantResolverTest.PathStrategyProfile.class)
class PathTenantResolverTest {

    @Test
    void resolvesTenantFromDefaultPathPattern() {
        when()
                .get("/t/acme/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[acme]"));
    }

    @Test
    void fallsBackWhenPathDoesNotMatchPattern() {
        when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[public]"));
    }

    @Test
    void rejectsReservedTenantFromPath() {
        when()
                .get("/t/__bootstrap/tenant")
                .then()
                .statusCode(400);
    }

    public static class PathStrategyProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.smallrye-jwt.enabled", "false",
                    "quarkus.multi-tenant.http.strategy", "path");
        }
    }
}
