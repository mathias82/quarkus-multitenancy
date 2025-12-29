package io.github.demo.resource;

import io.github.demo.resource.config.PostgresCheckProfile;
import io.github.demo.resource.utils.DatabaseTestUtils;
import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(PostgresCheckProfile.class)
public class UserResourceTest {

    @Inject
    TenantContext tenantContext;

    @BeforeAll
    static void beforeAll() {
        DatabaseTestUtils.assumePostgresAvailable("localhost", 5433);
        DatabaseTestUtils.assumePostgresAvailable("localhost", 5434);
    }

    @Test
    public void testTenantHeader() {
        RestAssured.given()
                .header("X-Tenant", "tenant1")
                .when().get("/api/users/tenant")
                .then()
                .statusCode(200)
                .body(equalTo("\"tenant1\""));
    }

    @Test
    public void testUserCreation() {
        RestAssured.given()
                .header("X-Tenant", "tenant1")
                .header("Content-Type", "application/json")
                .body("{\"name\":\"Alice\",\"email\":\"alice@a.com\"}")
                .when().post("/api/users")
                .then()
                .statusCode(200)
                .body("name", equalTo("Alice"));
    }
}
