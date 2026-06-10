package io.quarkiverse.multitenancy.orm.runtime.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * End-to-end coverage for the opt-out path
 * ({@code quarkus.multi-tenant.orm.header-filter.enabled=false}).
 *
 * <p>
 * Documents the scenario that motivated the flag: an application that uses one
 * of the non-header HTTP strategies (e.g. {@code path}, {@code jwt}, {@code
 * cookie}) but still wants the ORM module on the classpath for the
 * {@code OrmTenantResolverAdapter} bridge into Hibernate ORM. With the filter
 * disabled, requests without {@code X-Tenant} pass through instead of being
 * aborted with HTTP 400, and the filter does not overwrite whatever upstream
 * resolver has already populated on {@code TenantContext}.
 */
@QuarkusTest
@TestProfile(OrmTenantHeaderFilterDisabledTest.FilterDisabledProfile.class)
class OrmTenantHeaderFilterDisabledTest {

    @Test
    void missingHeader_passesThrough() {
        given()
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=(none)"));
    }

    @Test
    void headerPresent_isIgnored() {
        given()
                .header("X-Tenant", "ignored-by-opt-out")
                .when()
                .get("/echo")
                .then()
                .statusCode(200)
                .body(equalTo("tenant=(none)"));
    }

    public static class FilterDisabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.multi-tenant.orm.header-filter.enabled", "false");
        }
    }
}
