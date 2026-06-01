package io.quarkiverse.multitenancy.http.runtime.resolver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HeaderTenantResolverTest {

    @Test
    void resolvesTenantFromHeader() {
        given()
                .header("X-Tenant", "tenant-header")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-header]"));
    }
}
