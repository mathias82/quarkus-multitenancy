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
