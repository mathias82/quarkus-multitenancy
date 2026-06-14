package io.quarkiverse.multitenancy.http.runtime.validation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * End-to-end coverage for tenant-id hardening: a resolved identifier that
 * breaks the configured length or character-set policy is rejected with HTTP
 * 401 and never reaches the {@code TenantContext}.
 *
 * <p>
 * The profile caps the length at 16 so the over-length case is easy to express;
 * the character-set rule uses the shipped default {@code [A-Za-z0-9_-]+}.
 */
@QuarkusTest
@TestProfile(TenantIdValidationTest.HardenedHeaderProfile.class)
class TenantIdValidationTest {

    @Test
    void acceptsCompliantTenantId() {
        given()
                .header("X-Tenant", "acme-1")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[acme-1]"));
    }

    @Test
    void acceptsIdentifierAtTheLengthBoundary() {
        given()
                .header("X-Tenant", "0123456789abcdef") // exactly 16 characters
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[0123456789abcdef]"));
    }

    @Test
    void rejectsOverlongTenantId() {
        given()
                .header("X-Tenant", "0123456789abcdefg") // 17 characters, max is 16
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    @Test
    void rejectsDisallowedCharacters() {
        given()
                .header("X-Tenant", "acme;DROP") // ';' is outside [A-Za-z0-9_-]
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    /**
     * Caps tenant-id length at 16 and restricts the chain to {@code header} so
     * the test does not need to configure JWT verification.
     */
    public static class HardenedHeaderProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.multi-tenant.http.strategy", "header",
                    "quarkus.multi-tenant.http.tenant-id.max-length", "16");
        }
    }
}
