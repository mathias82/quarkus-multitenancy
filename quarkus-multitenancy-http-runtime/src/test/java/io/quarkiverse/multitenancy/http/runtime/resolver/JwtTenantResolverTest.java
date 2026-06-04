package io.quarkiverse.multitenancy.http.runtime.resolver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.http.runtime.config.SignedJwtSecurityProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.jwt.build.Jwt;

/**
 * Verifies that the JWT tenant strategy only resolves tenants from tokens that
 * pass SmallRye JWT signature verification, and that present-but-invalid
 * tokens never silently downgrade to the default tenant.
 */
@QuarkusTest
@TestProfile(SignedJwtSecurityProfile.class)
class JwtTenantResolverTest {

    private static final String ISSUER = "https://multitenancy.test/issuer";

    @Test
    void resolvesTenantFromVerifiedJwt() {
        String token = Jwt.issuer(ISSUER)
                .upn("alice")
                .claim("tenant", "tenant-jwt")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-jwt]"));
    }

    @Test
    void rejectsWithUnauthorizedWhenSignatureCannotBeVerified() {
        // Token signed with a wrong issuer fails SmallRye JWT verification.
        String token = Jwt.issuer("https://attacker.example/issuer")
                .upn("mallory")
                .claim("tenant", "attacker-tenant")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    @Test
    void rejectsWithUnauthorizedWhenTenantClaimMissing() {
        String token = Jwt.issuer(ISSUER)
                .upn("alice")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    @Test
    void rejectsWithUnauthorizedWhenBearerTokenMalformed() {
        given()
                .header("Authorization", "Bearer this.is.not.a.jwt")
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    @Test
    void fallsBackToDefaultTenantWhenNoBearerTokenPresent() {
        // No Bearer token → JwtTenantResolver returns NotApplicable, the chain
        // exhausts without a Rejected, and the filter falls back to the
        // configured default tenant ("public", from HttpTenantConfig defaults).
        given()
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(containsString("public"));
    }

    @Test
    void resolvesTenantWhenBearerSchemeUsesLowercase() {
        // HTTP auth-scheme tokens are case-insensitive (RFC 7235 §2.1). A
        // lowercase scheme must still hit the JWT path and never fall through
        // to NotApplicable — otherwise the default-tenant fallback would
        // silently downgrade a valid request.
        String token = Jwt.issuer(ISSUER)
                .upn("alice")
                .claim("tenant", "tenant-jwt")
                .sign();

        given()
                .header("Authorization", "bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-jwt]"));
    }

    @Test
    void rejectsWhenBearerSchemeIsPresentButTokenIsBlank() {
        // "Bearer " with no token, or just "Bearer", is malformed. We must
        // reject (401) instead of treating it as NotApplicable, otherwise a
        // misbehaving client can downgrade itself to the default tenant.
        given()
                .header("Authorization", "Bearer ")
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);

        given()
                .header("Authorization", "Bearer")
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    @Test
    void resolvesTenantTrimmingWhitespaceFromClaim() {
        // Other built-in resolvers trim resolved tenant ids — the JWT path
        // should too, so a token whose tenant claim has stray whitespace
        // still matches the configured tenant id.
        String token = Jwt.issuer(ISSUER)
                .upn("alice")
                .claim("tenant", "  tenant-jwt  ")
                .sign();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[tenant-jwt]"));
    }
}
