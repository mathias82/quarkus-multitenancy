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
package io.github.demo.resource;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

import io.github.demo.resource.config.TenantDatabasesTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@QuarkusTestResource(TenantDatabasesTestResource.class)
class DatabaseRoutingScenariosTest {

    @Test
    void headerRoutesToEachDatabase() {
        assertHeader("tenant1", "tenant1@example.com", "tenant2@example.com");
        assertHeader("tenant2", "tenant2@example.com", "tenant1@example.com");
    }

    @Test
    void cookieRoutesToTenant2Database() {
        assertTenantData(RestAssured.given().cookie("tenant_cookie", "tenant2"), "/api/users",
                "tenant2@example.com", "tenant1@example.com");
    }

    @Test
    void pathRoutesToTenant2Database() {
        assertTenantData(RestAssured.given(), "/api/users/path/tenant2",
                "tenant2@example.com", "tenant1@example.com");
    }

    @Test
    void defaultRoutesToTenant1Database() {
        assertTenantData(RestAssured.given(), "/api/users",
                "tenant1@example.com", "tenant2@example.com");
    }

    @Test
    void headerWinsOverCookieForDatabaseRouting() {
        assertTenantData(RestAssured.given().header("X-Tenant", "tenant1").cookie("tenant_cookie", "tenant2"),
                "/api/users", "tenant1@example.com", "tenant2@example.com");
    }

    private void assertHeader(String tenant, String expected, String unexpected) {
        assertTenantData(RestAssured.given().header("X-Tenant", tenant), "/api/users", expected, unexpected);
    }

    private void assertTenantData(io.restassured.specification.RequestSpecification request,
            String path, String expected, String unexpected) {
        request.when().get(path)
                .then().statusCode(200)
                .body("email", hasItem(expected))
                .body("email", not(hasItem(unexpected)));
    }
}
