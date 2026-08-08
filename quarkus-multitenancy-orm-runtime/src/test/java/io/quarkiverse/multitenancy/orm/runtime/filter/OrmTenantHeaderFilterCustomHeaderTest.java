/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package io.quarkiverse.multitenancy.orm.runtime.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(OrmTenantHeaderFilterCustomHeaderTest.CustomHeaderProfile.class)
class OrmTenantHeaderFilterCustomHeaderTest {

    @Test
    void configuredHeaderIsUsed() {
        given()
                .header("X-Org-Tenant", "acme")
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=acme"));
    }

    @Test
    void legacyHeaderDoesNotOverrideConfiguredHeaderName() {
        given()
                .header("X-Tenant", "legacy")
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }

    public static class CustomHeaderProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.multi-tenant.orm.header-filter.header-name", "X-Org-Tenant");
        }
    }
}
