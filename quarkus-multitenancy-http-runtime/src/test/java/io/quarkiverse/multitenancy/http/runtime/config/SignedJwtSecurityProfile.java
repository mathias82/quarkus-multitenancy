package io.quarkiverse.multitenancy.http.runtime.config;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Wires SmallRye JWT to verify tokens with the committed test public key. The
 * matching private key is used by tests to mint tokens via
 * {@code io.smallrye.jwt.build.Jwt}.
 */
public class SignedJwtSecurityProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "mp.jwt.verify.publickey.location", "test-public-key.pem",
                "mp.jwt.verify.publickey.algorithm", "RS256",
                "mp.jwt.verify.issuer", "https://multitenancy.test/issuer",
                "smallrye.jwt.sign.key.location", "test-private-key.pem");
    }
}
