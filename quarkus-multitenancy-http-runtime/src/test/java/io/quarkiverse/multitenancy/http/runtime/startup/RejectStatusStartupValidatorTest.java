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

/**
 * Unit-tests the range check of {@link RejectStatusStartupValidator} without
 * spinning up a full Quarkus application per scenario, mirroring
 * {@code JwtStrategyStartupValidatorTest}.
 */
class RejectStatusStartupValidatorTest {

    @Test
    void acceptsLowerBoundary() {
        assertDoesNotThrow(() -> validatorWith(400).onStart(null));
    }

    @Test
    void acceptsDefaultUnauthorized() {
        assertDoesNotThrow(() -> validatorWith(401).onStart(null));
    }

    @Test
    void acceptsUpperBoundary() {
        assertDoesNotThrow(() -> validatorWith(499).onStart(null));
    }

    @Test
    void rejectsOk() {
        assertThrows(IllegalStateException.class, () -> validatorWith(200).onStart(null));
    }

    @Test
    void rejectsRedirect() {
        assertThrows(IllegalStateException.class, () -> validatorWith(302).onStart(null));
    }

    @Test
    void rejectsJustBelowLowerBoundary() {
        assertThrows(IllegalStateException.class, () -> validatorWith(399).onStart(null));
    }

    @Test
    void rejectsServerError() {
        assertThrows(IllegalStateException.class, () -> validatorWith(500).onStart(null));
    }

    @Test
    void rejectsOutOfHttpRange() {
        assertThrows(IllegalStateException.class, () -> validatorWith(600).onStart(null));
    }

    @Test
    void rejectsNegative() {
        assertThrows(IllegalStateException.class, () -> validatorWith(-1).onStart(null));
    }

    @Test
    void doesNothingWhenExtensionDisabled() {
        // An invalid status must not fail boot when the filter is off entirely.
        assertDoesNotThrow(() -> newValidator(fakeConfig(false, true, 500)).onStart(null));
    }

    @Test
    void doesNothingWhenValidationDisabled() {
        // reject-status is inert when tenant-id validation is off, so an
        // out-of-range value must not fail boot.
        assertDoesNotThrow(() -> newValidator(fakeConfig(true, false, 500)).onStart(null));
    }

    @Test
    void errorMessageGuidesTheOperator() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> validatorWith(500).onStart(null));

        String message = thrown.getMessage();
        assertTrue(message.contains("quarkus.multi-tenant.http.tenant-id.reject-status"));
        assertTrue(message.contains("500"));
        assertTrue(message.contains("[400,499]"));
    }

    private static RejectStatusStartupValidator validatorWith(int rejectStatus) {
        return newValidator(fakeConfig(true, true, rejectStatus));
    }

    private static RejectStatusStartupValidator newValidator(HttpTenantConfig httpConfig) {
        RejectStatusStartupValidator v = new RejectStatusStartupValidator();
        v.config = httpConfig;
        return v;
    }

    private static HttpTenantConfig fakeConfig(boolean enabled, boolean validationEnabled, int rejectStatus) {
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
                        return 64;
                    }

                    @Override
                    public String pattern() {
                        return "[A-Za-z0-9_-]+";
                    }

                    @Override
                    public int rejectStatus() {
                        return rejectStatus;
                    }
                };
            }
        };
    }
}
