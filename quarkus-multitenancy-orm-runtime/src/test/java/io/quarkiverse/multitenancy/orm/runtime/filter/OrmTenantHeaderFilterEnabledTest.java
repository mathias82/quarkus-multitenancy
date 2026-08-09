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
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * End-to-end integration coverage for {@link OrmTenantHeaderFilter} with the
 * default configuration ({@code quarkus.multi-tenant.orm.header-filter.enabled=true}).
 *
 * <p>
 * The pre-existing {@link OrmTenantHeaderFilterTest} constructs the filter with
 * {@code new OrmTenantHeaderFilter()} and assigns its fields by hand, so it
 * never exercises the JAX-RS or CDI integration path. This {@code @QuarkusTest}
 * boots a real RESTEasy + ArC stack so any regression in the filter's wiring
 * (a removed {@code @ApplicationScoped}, a stale {@code @Inject}, a broken
 * config binding) is caught here.
 */
@QuarkusTest
class OrmTenantHeaderFilterEnabledTest {

    @Test
    void validHeader_returnsTenant() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=acme"));
    }

    @Test
    void missingHeader_returns400() {
        given()
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }

    @Test
    void blankHeader_returns400() {
        given()
                .header("X-Tenant", "  ")
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }

    @Test
    void reservedHeader_returns400() {
        given()
                .header("X-Tenant", "__bootstrap")
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }

    @Test
    void invalidHeader_returns400() {
        given()
                .header("X-Tenant", "acme;DROP")
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }

    @Test
    void fallbackRunsAfterAuthenticationPriority() {
        Priority priority = OrmTenantHeaderFilter.class.getAnnotation(Priority.class);
        assertEquals(Priorities.AUTHENTICATION + 100, priority.value());
    }
}
