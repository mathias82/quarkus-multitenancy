package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

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
