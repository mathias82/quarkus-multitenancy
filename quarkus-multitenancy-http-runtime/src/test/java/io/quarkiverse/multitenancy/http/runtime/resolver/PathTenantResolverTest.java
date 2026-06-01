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

    public static class PathStrategyProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.smallrye-jwt.enabled", "false",
                    "quarkus.multi-tenant.http.strategy", "path");
        }
    }
}
