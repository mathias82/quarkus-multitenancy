package io.quarkiverse.multitenancy.http.runtime.startup;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.microprofile.config.Config;
import org.jboss.logmanager.LogManager;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * Unit-tests the warning emission of {@link ImplicitDefaultStrategyWarner}.
 *
 * <p>
 * The validator is wired up by hand so each scenario can vary the
 * {@link Config} and {@link HttpTenantConfig} pair independently. A captured
 * {@link Handler} attached to the validator's logger lets the assertions tell
 * "warning emitted" apart from "warning suppressed" without inspecting global
 * log output.
 */
class ImplicitDefaultStrategyWarnerTest {

    private static Logger targetLogger;
    private CapturingHandler capture;

    @BeforeAll
    static void installLogManager() {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        targetLogger = Logger.getLogger(ImplicitDefaultStrategyWarner.class.getName());
    }

    @BeforeEach
    void attachCapture() {
        capture = new CapturingHandler();
        capture.setLevel(Level.ALL);
        targetLogger.addHandler(capture);
        targetLogger.setUseParentHandlers(false);
    }

    @AfterEach
    void detachCapture() {
        targetLogger.removeHandler(capture);
        targetLogger.setUseParentHandlers(true);
    }

    @Test
    void warnsWhenJwtInImplicitDefaultStrategy() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(true, List.of("header", "jwt", "cookie")),
                mpConfig(Map.of()));

        assertDoesNotThrow(() -> w.onStart(null));
        assertTrue(capture.sawWarningContaining("implicit default chain"),
                "Expected a warning about the implicit default chain but got none.");
    }

    @Test
    void doesNotWarnWhenStrategyExplicitlySetEvenToSameValue() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(true, List.of("header", "jwt", "cookie")),
                mpConfig(Map.of("quarkus.multi-tenant.http.strategy", "header,jwt,cookie")));

        assertDoesNotThrow(() -> w.onStart(null));
        assertFalse(capture.anyWarning(),
                "Explicit configuration must not trigger the warning, even if it matches the default.");
    }

    @Test
    void doesNotWarnWhenJwtNotInStrategy() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(true, List.of("header", "cookie")),
                mpConfig(Map.of()));

        assertDoesNotThrow(() -> w.onStart(null));
        assertFalse(capture.anyWarning());
    }

    @Test
    void doesNotWarnWhenExtensionDisabled() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(false, List.of("header", "jwt", "cookie")),
                mpConfig(Map.of()));

        assertDoesNotThrow(() -> w.onStart(null));
        assertFalse(capture.anyWarning());
    }

    @Test
    void warnsForMixedCaseJwtToken() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(true, List.of("Header", "JWT", "Cookie")),
                mpConfig(Map.of()));

        assertDoesNotThrow(() -> w.onStart(null));
        assertTrue(capture.sawWarningContaining("implicit default chain"));
    }

    /**
     * Regression for the production-only bug surfaced while exercising the
     * extension in a real Quarkus application: SmallRye Config exposes
     * {@code @WithDefault} values through a synthetic {@code DefaultValuesConfigSource}
     * pinned at {@link Integer#MIN_VALUE} ordinal. A naive iteration of
     * {@link Config#getPropertyNames()} therefore sees the strategy key even when
     * the user never set it, which would silence the warning in the very case it
     * exists to flag. The warner now uses the {@link io.smallrye.config.ConfigValue}
     * ordinal to filter that source out, and this test pins that behaviour by
     * registering a source that mimics the default-values one.
     */
    @Test
    void warnsEvenWhenDefaultValuesSourceExposesTheKey() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(true, List.of("header", "jwt", "cookie")),
                mpConfigWithDefaultValuesSource("header,jwt,cookie"));

        assertDoesNotThrow(() -> w.onStart(null));
        assertTrue(capture.sawWarningContaining("implicit default chain"),
                "A property surfaced only by the default-values source must still be treated as implicit.");
    }

    @Test
    void doesNotWarnWhenUserSourceOverridesDefaultValuesSource() {
        ImplicitDefaultStrategyWarner w = newWarner(
                fakeConfig(true, List.of("header", "jwt", "cookie")),
                mpConfigWithDefaultValuesAndOverride("header,jwt,cookie", "header,jwt,cookie"));

        assertDoesNotThrow(() -> w.onStart(null));
        assertFalse(capture.anyWarning(),
                "An explicit user source must win over the default-values source even when the value matches.");
    }

    private static ImplicitDefaultStrategyWarner newWarner(HttpTenantConfig httpConfig, Config mpConfig) {
        ImplicitDefaultStrategyWarner w = new ImplicitDefaultStrategyWarner();
        w.config = httpConfig;
        w.mpConfig = mpConfig;
        return w;
    }

    private static Config mpConfig(Map<String, String> values) {
        return new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(values, "test", 100))
                .build();
    }

    private static Config mpConfigWithDefaultValuesSource(String strategyValue) {
        return new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(
                        Map.of("quarkus.multi-tenant.http.strategy", strategyValue),
                        "DefaultValuesConfigSource",
                        Integer.MIN_VALUE))
                .build();
    }

    private static Config mpConfigWithDefaultValuesAndOverride(String defaultValue, String userValue) {
        return new SmallRyeConfigBuilder()
                .withSources(
                        new PropertiesConfigSource(
                                Map.of("quarkus.multi-tenant.http.strategy", defaultValue),
                                "DefaultValuesConfigSource",
                                Integer.MIN_VALUE),
                        new PropertiesConfigSource(
                                Map.of("quarkus.multi-tenant.http.strategy", userValue),
                                "application.properties",
                                250))
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

            @Override
            public JwtConfig jwt() {
                return () -> false;
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

    private static final class CapturingHandler extends Handler {
        private final java.util.List<LogRecord> records = new java.util.ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        boolean anyWarning() {
            return records.stream().anyMatch(r -> r.getLevel().intValue() >= Level.WARNING.intValue());
        }

        boolean sawWarningContaining(String fragment) {
            return records.stream()
                    .filter(r -> r.getLevel().intValue() >= Level.WARNING.intValue())
                    .anyMatch(r -> renderedMessage(r).contains(fragment));
        }

        private static String renderedMessage(LogRecord record) {
            String msg = record.getMessage();
            if (msg == null) {
                return "";
            }
            Object[] params = record.getParameters();
            if (params == null || params.length == 0) {
                return msg;
            }
            try {
                return String.format(msg.replace("%s", "%s"), params);
            } catch (RuntimeException e) {
                return msg;
            }
        }
    }
}
