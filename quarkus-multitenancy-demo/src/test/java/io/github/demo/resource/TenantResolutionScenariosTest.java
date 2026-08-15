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

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.github.demo.resource.config.HttpOnlyProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(HttpOnlyProfile.class)
class TenantResolutionScenariosTest {

    @Test
    void resolvesHeader() {
        RestAssured.given().header("X-Tenant", "tenant1")
                .when().get("/api/users/tenant")
                .then().statusCode(200).body(equalTo("\"tenant1\""));
    }

    @Test
    void resolvesCookie() {
        RestAssured.given().cookie("tenant_cookie", "tenant2")
                .when().get("/api/users/tenant")
                .then().statusCode(200).body(equalTo("\"tenant2\""));
    }

    @Test
    void resolvesPath() {
        RestAssured.when().get("/api/users/tenant/path/tenant2")
                .then().statusCode(200).body(equalTo("\"tenant2\""));
    }

    @Test
    void usesDefaultTenant() {
        RestAssured.when().get("/api/users/tenant")
                .then().statusCode(200).body(equalTo("\"tenant1\""));
    }

    @Test
    void headerPrecedesCookie() {
        RestAssured.given().header("X-Tenant", "tenant1").cookie("tenant_cookie", "tenant2")
                .when().get("/api/users/tenant")
                .then().statusCode(200).body(equalTo("\"tenant1\""));
    }
}
