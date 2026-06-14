/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                fakeConfig(true, List.of("jwt"), true),
                mpConfig(Map.of()));
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
    void passesWhenOidcTenantSpecificAuthServerConfigured() {
        // Multi-tenant OIDC apps configure quarkus.oidc.<tenant>.auth-server-url
        // rather than the unqualified property. The validator must walk the
        // resolved property names to detect those cases.
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "quarkus.oidc.acme.auth-server-url", "https://acme.example.com")));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void ignoresUnrelatedOidcSubKeys() {
        // Properties under quarkus.oidc.* that are NOT auth-server-url should
        // not be treated as a verification source.
        JwtStrategyStartupValidator v = newValidator(
                fakeConfig(true, List.of("jwt")),
                mpConfig(Map.of(
                        "quarkus.oidc.client-id", "ignored",
                        "quarkus.oidc.acme.client-id", "also-ignored")));
        assertThrows(IllegalStateException.class, () -> v.onStart(null));
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
        return fakeConfig(enabled, strategy, false);
    }

    private static HttpTenantConfig fakeConfig(boolean enabled, List<String> strategy, boolean skipStartupCheck) {
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

            @Override
            public JwtConfig jwt() {
                return () -> skipStartupCheck;
            }

            @Override
            public TenantIdConfig tenantId() {
                return new TenantIdConfig() {
                    @Override
                    public boolean validationEnabled() {
                        return true;
                    }

                    @Override
                    public int maxLength() {
                        return 64;
                    }

                    @Override
                    public String pattern() {
                        return "[A-Za-z0-9_-]+";
                    }
                };
            }
        };
    }
}
