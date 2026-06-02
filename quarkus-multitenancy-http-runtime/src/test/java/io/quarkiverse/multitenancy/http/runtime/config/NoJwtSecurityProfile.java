package io.quarkiverse.multitenancy.http.runtime.config;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class NoJwtSecurityProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.smallrye-jwt.enabled", "false");
    }
}
