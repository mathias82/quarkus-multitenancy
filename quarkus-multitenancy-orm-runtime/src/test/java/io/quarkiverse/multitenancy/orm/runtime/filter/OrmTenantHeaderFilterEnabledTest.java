package io.quarkiverse.multitenancy.orm.runtime.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * End-to-end integration coverage for {@link OrmTenantHeaderFilter} with the
 * default configuration ({@code quarkus.multi-tenant.orm.header-filter.enabled=true}).
 *
 * <p>
 * The pre-existing {@link OrmTenantHeaderFilterTest} constructs the filter with
 * {@code new OrmTenantHeaderFilter()} and assigns its fields by hand, so it
 * never exercises the JAX-RS or CDI integration path. This {@code @QuarkusTest}
 * boots a real RESTEasy + ArC stack so any regression in the filter's wiring
 * (a removed {@code @ApplicationScoped}, a stale {@code @Inject}, a broken
 * config binding) is caught here.
 */
@QuarkusTest
class OrmTenantHeaderFilterEnabledTest {

    @Test
    void validHeader_returnsTenant() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=acme"));
    }

    @Test
    void missingHeader_returns400() {
        given()
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }

    @Test
    void blankHeader_returns400() {
        given()
                .header("X-Tenant", "  ")
                .when()
                .get("/echo")
                .then()
                .statusCode(400);
    }
}
