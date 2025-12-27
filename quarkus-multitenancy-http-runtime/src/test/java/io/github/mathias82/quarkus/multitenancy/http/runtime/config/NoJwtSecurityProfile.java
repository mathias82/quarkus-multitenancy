package io.github.mathias82.quarkus.multitenancy.http.runtime.config;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class NoJwtSecurityProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.smallrye-jwt.enabled", "false"
        );
    }
}
