package io.quarkiverse.multitenancy.http.runtime.startup;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * Unit-tests the conservative-trigger logic of
 * {@link JwtStrategyStartupValidator} without spinning up a full Quarkus
 * application per scenario.
 */
class JwtStrategyStartupValidatorTest {

    @Test
    void doesNothingWhenExtensionDisabled() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(false, List.of("jwt")),
                mpConfig(Map.of()));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void doesNothingWhenJwtNotInStrategy() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("header", "cookie")),
                mpConfig(Map.of()));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void doesNothingWhenSkipFlagSet() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "quarkus.multi-tenant.http.jwt.skip-startup-check", "true")));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void passesWhenSmallryeJwtPublicKeyLocationConfigured() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "mp.jwt.verify.publickey.location", "publicKey.pem")));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void passesWhenSmallryeJwtInlinePublicKeyConfigured() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "mp.jwt.verify.publickey", "-----BEGIN PUBLIC KEY-----...")));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void passesWhenOidcAuthServerConfigured() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "quarkus.oidc.auth-server-url", "https://issuer.example.com")));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void failsWhenJwtEnabledWithoutVerificationSource() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("header", "jwt", "cookie")),
                mpConfig(Map.of()));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> v.onStart(null));

        // The error message must guide the operator: name the offending config
        // option(s) and offer the opt-out flag.
        String message = thrown.getMessage();
        assertTrue(message.contains("jwt"));
        assertTrue(message.contains("mp.jwt.verify.publickey.location"));
        assertTrue(message.contains("quarkus.oidc.auth-server-url"));
        assertTrue(message.contains("skip-startup-check"));
    }

    @Test
    void failsWhenVerificationKeyIsBlank() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "mp.jwt.verify.publickey.location", "   ")));
        assertThrows(IllegalStateException.class, () -> v.onStart(null));
    }

    @Test
    void acceptsMixedCaseJwtStrategyValue() {
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("Header", "JWT", "Cookie")),
                mpConfig(Map.of()));
        assertThrows(IllegalStateException.class, () -> v.onStart(null));
    }

    private static JwtStrategyStartupValidator newValidator(HttpTenantConfig httpConfig, Config mpConfig) {
        JwtStrategyStartupValidator v = new JwtStrategyStartupValidator();
        v.config = httpConfig;
        v.mpConfig = mpConfig;
        return v;
    }

    private static Config mpConfig(Map<String, String> values) {
        return new SmallRyeConfigBuilder()
                .withSources(new io.smallrye.config.PropertiesConfigSource(values, "test", 100))
                .build();
    }

    private static HttpTenantConfig fakeConfig(boolean enabled, List<String> strategy) {
        return new HttpTenantConfig() {
            @Override
            public boolean enabled() {
                return enabled;
            }

            @Override
            public List<String> strategy() {
                return strategy;
            }

            @Override
            public String headerName() {
                return "X-Tenant";
            }

            @Override
            public String jwtClaimName() {
                return "tenant";
            }

            @Override
            public String cookieName() {
                return "tenant_cookie";
            }

            @Override
            public String defaultTenant() {
                return "public";
            }

            @Override
            public String pathPattern() {
                return "^/t/([^/]+)(?:/|$)";
            }

            @Override
            public int pathGroup() {
                return 1;
            }
        };
    }
}
