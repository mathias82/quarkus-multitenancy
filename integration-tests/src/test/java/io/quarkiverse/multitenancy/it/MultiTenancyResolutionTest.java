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
package io.quarkiverse.multitenancy.it;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

/**
 * Exercises the HTTP tenant resolution chain end to end against the running application. This
 * class runs in the JVM (surefire); the {@code *IT} subclass replays the same assertions against
 * the native binary, so the paths that matter in production are verified to behave identically in
 * native mode: header resolution, verified-JWT resolution, tenant-id validation, default-tenant
 * fallback, rejected-resolution handling, and actual Hibernate ORM datasource routing.
 */
@QuarkusTest
class MultiTenancyResolutionTest {

    private static final String ISSUER = "https://multitenancy.test/issuer";

    @Test
    void resolvesTenantFromHeader() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("acme"));
    }

    @Test
    void resolvesTenantFromVerifiedJwt() {
        String token = Jwt.issuer(ISSUER)
                .upn("alice")
                .claim("tenant", "jwt-tenant")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("jwt-tenant"));
    }

    @Test
    void rejectsBootstrapTenantFromHeader() {
        given()
                .header("X-Tenant", "__bootstrap")
                .when()
                .get("/tenant")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsBootstrapTenantFromCookie() {
        given()
                .cookie("tenant_cookie", "__bootstrap")
                .when()
                .get("/tenant")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsBootstrapTenantFromPath() {
        when()
                .get("/t/__bootstrap/tenant")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsBootstrapTenantFromVerifiedJwt() {
        String token = Jwt.issuer(ISSUER)
                .upn("alice")
                .claim("tenant", "__bootstrap")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(400);
    }

    @Test
    void ormKeepsTenantDataIsolated() {
        given()
                .header("X-Tenant", "tenant1")
                .when()
                .post("/orm/tenant-one")
                .then()
                .statusCode(204);

        given()
                .header("X-Tenant", "tenant2")
                .when()
                .post("/orm/tenant-two")
                .then()
                .statusCode(204);

        given()
                .header("X-Tenant", "tenant1")
                .when()
                .get("/orm")
                .then()
                .statusCode(200)
                .body(is("tenant-one"));

        given()
                .header("X-Tenant", "tenant2")
                .when()
                .get("/orm")
                .then()
                .statusCode(200)
                .body(is("tenant-two"));
    }

    @Test
    void rejectsBootstrapTenantBeforeOrmAccess() {
        given()
                .header("X-Tenant", "__bootstrap")
                .when()
                .get("/orm")
                .then()
                .statusCode(400);
    }

    @Test
    void fallsBackToDefaultTenantWhenNothingResolves() {
        // No header, bearer token or cookie: the chain exhausts without a match and the filter
        // applies the configured default tenant ("public", from HttpTenantConfig defaults).
        when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("public"));
    }

    @Test
    void rejectsTenantIdThatViolatesThePolicy() {
        // ';' is outside the default [A-Za-z0-9_-]+ character set, so validation rejects the
        // request with the default reject status (400) before it reaches the resource.
        given()
                .header("X-Tenant", "acme;DROP")
                .when()
                .get("/tenant")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsOverlongTenantId() {
        // 17 characters, over the configured max-length of 16: validation rejects it with 400
        // before it reaches the resource. This exercises the length branch of the policy.
        given()
                .header("X-Tenant", "0123456789abcdefg")
                .when()
                .get("/tenant")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsRequestWhenJwtSignatureCannotBeVerified() {
        // A token signed for a different issuer fails SmallRye JWT verification. The JWT resolver
        // rejects it (401) instead of silently downgrading to the default tenant.
        String token = Jwt.issuer("https://attacker.example/issuer")
                .upn("mallory")
                .claim("tenant", "attacker")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }
}
