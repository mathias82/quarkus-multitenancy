package io.quarkiverse.multitenancy.http.runtime.validation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * Confirms the hardening gate is an opt-out: with
 * {@code quarkus.multi-tenant.http.tenant-id.validation-enabled=false} a value
 * that would otherwise be rejected (disallowed characters) flows through to the
 * {@code TenantContext} unchanged.
 */
@QuarkusTest
@TestProfile(TenantIdValidationDisabledTest.ValidationDisabledProfile.class)
class TenantIdValidationDisabledTest {

    @Test
    void passesThroughWhenValidationDisabled() {
        given()
                .header("X-Tenant", "acme;DROP")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[acme;DROP]"));
    }

    public static class ValidationDisabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.multi-tenant.http.strategy", "header",
                    "quarkus.multi-tenant.http.tenant-id.validation-enabled", "false");
        }
    }
}
