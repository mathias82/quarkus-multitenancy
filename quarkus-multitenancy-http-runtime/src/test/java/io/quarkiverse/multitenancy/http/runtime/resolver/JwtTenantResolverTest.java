package io.quarkiverse.multitenancy.http.runtime.resolver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.http.runtime.config.NoJwtSecurityProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(NoJwtSecurityProfile.class)
class JwtTenantResolverTest {

    @Test
    void resolvesTenantFromJwt() {
        String jwt = jwtWithTenant();

        given()
                .header("Authorization", "Bearer " + jwt)
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-jwt]"));
    }

    private String jwtWithTenant() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes());

        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"tenant\":\"" + "tenant-jwt" + "\"}").getBytes());

        return header + "." + payload + ".";
    }
}
