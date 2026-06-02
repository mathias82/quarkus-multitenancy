package io.quarkiverse.multitenancy.http.runtime.resolver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CookieTenantResolverTest {

    @Test
    void resolvesTenantFromCookie() {
        given()
                .cookie("tenant_cookie", "tenant-cookie")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-cookie]"));
    }
}
