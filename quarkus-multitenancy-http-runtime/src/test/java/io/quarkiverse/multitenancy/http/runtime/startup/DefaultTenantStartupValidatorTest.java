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

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.validation.TenantIdValidator;

/**
 * Unit-tests the fail-fast logic of {@link DefaultTenantStartupValidator}
 * against a real {@link TenantIdValidator}, without spinning up a full Quarkus
 * application per scenario. Wiring a real validator (rather than a stub) keeps
 * the test honest about the policy the production filter actually enforces.
 */
class DefaultTenantStartupValidatorTest {

    private static final String DEFAULT_PATTERN = "[A-Za-z0-9_-]+";
    private static final int DEFAULT_MAX_LENGTH = 64;

    @Test
    void doesNothingWhenExtensionDisabled() {
        // A bad default must not fail boot when the filter itself is off.
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(false, "my@tenant", true, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void passesForDefaultPublicTenant() {
        // The @WithDefault("public") value must keep booting out of the box.
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(true, "public", true, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void passesWhenValidationDisabled() {
        // Disabling validation must skip the default-tenant check too, or the
        // static fallback would be held to a stricter standard than live ids.
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(true, "my@tenant", false, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));
        assertDoesNotThrow(() -> v.onStart(null));
    }

    @Test
    void rejectsReservedDefaultTenantWhenValidationDisabled() {
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(true, "__bootstrap", false, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> v.onStart(null));

        assertTrue(thrown.getMessage().contains("reserved"));
    }

    @Test
    void failsWhenDefaultTenantViolatesPattern() {
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(true, "my@tenant", true, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> v.onStart(null));

        String message = thrown.getMessage();
        assertTrue(message.contains(DefaultTenantStartupValidator.DEFAULT_TENANT_PROPERTY),
                "message must name the offending property");
        assertTrue(message.contains("pattern"), "message must explain the violated rule");
    }

    @Test
    void failsWhenDefaultTenantExceedsMaxLength() {
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(true, "a".repeat(DEFAULT_MAX_LENGTH + 1), true, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> v.onStart(null));
        assertTrue(thrown.getMessage().contains("length"), "message must explain the length rule");
    }

    @Test
    void failsWhenDefaultTenantIsEmpty() {
        // An empty fallback tenant fails the default pattern (>= 1 char) and is
        // an intentional misconfiguration to surface at boot.
        DefaultTenantStartupValidator v = newValidator(
                fakeConfig(true, "", true, DEFAULT_MAX_LENGTH, DEFAULT_PATTERN));
        assertThrows(IllegalStateException.class, () -> v.onStart(null));
    }

    private static DefaultTenantStartupValidator newValidator(HttpTenantConfig httpConfig) {
        DefaultTenantStartupValidator v = new DefaultTenantStartupValidator();
        v.config = httpConfig;
        v.validator = new TenantIdValidator(httpConfig);
        return v;
    }

    private static HttpTenantConfig fakeConfig(boolean enabled, String defaultTenant, boolean validationEnabled,
            int maxLength, String pattern) {
        return new HttpTenantConfig() {
            @Override
            public boolean enabled() {
                return enabled;
            }

            @Override
            public List<String> strategy() {
                return List.of("header", "cookie");
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
                return defaultTenant;
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
                return () -> false;
            }

            @Override
            public TenantIdConfig tenantId() {
                return new TenantIdConfig() {
                    @Override
                    public boolean validationEnabled() {
                        return validationEnabled;
                    }

                    @Override
                    public int maxLength() {
                        return maxLength;
                    }

                    @Override
                    public String pattern() {
                        return pattern;
                    }

                    @Override
                    public int rejectStatus() {
                        return 400;
                    }
                };
            }
        };
    }
}
