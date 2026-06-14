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
package io.quarkiverse.multitenancy.orm.runtime.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * End-to-end coverage for the opt-out path
 * ({@code quarkus.multi-tenant.orm.header-filter.enabled=false}).
 *
 * <p>
 * Documents the scenario that motivated the flag: an application that uses one
 * of the non-header HTTP strategies (e.g. {@code path}, {@code jwt}, {@code
 * cookie}) but still wants the ORM module on the classpath for the
 * {@code OrmTenantResolverAdapter} bridge into Hibernate ORM. With the filter
 * disabled, requests without {@code X-Tenant} pass through instead of being
 * aborted with HTTP 400, and the filter does not overwrite whatever upstream
 * resolver has already populated on {@code TenantContext}.
 */
@QuarkusTest
@TestProfile(OrmTenantHeaderFilterDisabledTest.FilterDisabledProfile.class)
class OrmTenantHeaderFilterDisabledTest {

    @Test
    void missingHeader_passesThrough() {
        given()
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=(none)"));
    }

    @Test
    void headerPresent_isIgnored() {
        given()
                .header("X-Tenant", "ignored-by-opt-out")
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=(none)"));
    }

    public static class FilterDisabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.multi-tenant.orm.header-filter.enabled", "false");
        }
    }
}
