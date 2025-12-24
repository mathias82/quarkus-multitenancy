package io.github.demo.resource;

import io.github.demo.entity.User;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class UserResourceTest {

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
