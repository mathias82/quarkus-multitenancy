package io.quarkiverse.multitenancy.http.runtime.resolver;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DefaultTenantFallbackTest {

    @Test
    void fallsBackToDefaultTenantWhenNoResolverMatches() {
        when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[public]"));
    }
}
