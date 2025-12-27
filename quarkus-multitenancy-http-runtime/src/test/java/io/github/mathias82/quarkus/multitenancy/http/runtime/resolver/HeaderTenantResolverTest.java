package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

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
