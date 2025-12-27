package io.github.mathias82.quarkus.multitenancy.http.runtime.resolver;

import io.github.mathias82.quarkus.multitenancy.http.runtime.config.NoJwtSecurityProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

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
