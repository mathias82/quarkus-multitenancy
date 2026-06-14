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
package io.quarkiverse.multitenancy.http.runtime.validation;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;

/**
 * Validates a resolved tenant identifier against the configured length and
 * character-set policy before it is published to the
 * {@code TenantContext}.
 *
 * <p>
 * The validator is the single hardening gate for every resolver: built-in and
 * custom {@code TenantResolver}s alike produce their {@code Resolved} outcome
 * through the HTTP filter, which consults this bean before accepting the
 * identifier. A violating identifier is rejected (HTTP 401) rather than allowed
 * to flow downstream into log lines, SQL parameters, or ORM tenant lookups.
 *
 * <p>
 * The configured {@link Pattern} is compiled once at construction time, so
 * per-request validation is a single bounded scan with no recompilation.
 */
@ApplicationScoped
public class TenantIdValidator {

    /** Upper bound on how much of a rejected identifier is echoed into a log line. */
    private static final int LOG_PREVIEW_MAX_LENGTH = 64;

    /** Replacement rendered in place of a control character when sanitizing for logs. */
    private static final char SANITIZED_CONTROL_CHAR = '�';

    private final boolean enabled;
    private final int maxLength;
    private final String patternSource;
    private final Pattern pattern;

    @Inject
    public TenantIdValidator(HttpTenantConfig config) {
        HttpTenantConfig.TenantIdConfig tenantId = config.tenantId();
        this.enabled = tenantId.validationEnabled();
        this.maxLength = tenantId.maxLength();
        this.patternSource = tenantId.pattern();
        try {
            this.pattern = Pattern.compile(tenantId.pattern());
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException(
                    "Invalid quarkus.multi-tenant.http.tenant-id.pattern: '" + tenantId.pattern() + "'", e);
        }
    }

    /**
     * Checks a resolved tenant identifier against the configured policy.
     *
     * @param tenantId the identifier produced by a resolver; must not be
     *        {@code null}
     * @return a log-safe rejection reason when the identifier violates the
     *         policy, or {@link Optional#empty()} when it is acceptable (or
     *         validation is disabled)
     */
    public Optional<String> validate(String tenantId) {
        if (!enabled) {
            return Optional.empty();
        }
        int length = tenantId.length();
        if (length > maxLength) {
            return Optional.of(String.format(
                    "length %d exceeds the configured maximum of %d (sanitized: '%s')",
                    length, maxLength, sanitizeForLog(tenantId)));
        }
        if (!pattern.matcher(tenantId).matches()) {
            return Optional.of(String.format(
                    "does not match the configured pattern '%s' (sanitized: '%s')",
                    patternSource, sanitizeForLog(tenantId)));
        }
        return Optional.empty();
    }

    /**
     * Renders an untrusted identifier safe to place in a log line: replaces
     * control characters (including the CR/LF that would otherwise enable log
     * injection) with a placeholder and truncates to
     * {@value #LOG_PREVIEW_MAX_LENGTH} characters.
     *
     * @param raw the untrusted value; must not be {@code null}
     * @return a single-line, length-bounded rendering of {@code raw}
     */
    public static String sanitizeForLog(String raw) {
        int limit = Math.min(raw.length(), LOG_PREVIEW_MAX_LENGTH);
        StringBuilder sb = new StringBuilder(limit + 1);
        for (int i = 0; i < limit; i++) {
            char c = raw.charAt(i);
            sb.append(Character.isISOControl(c) ? SANITIZED_CONTROL_CHAR : c);
        }
        if (raw.length() > LOG_PREVIEW_MAX_LENGTH) {
            sb.append('…');
        }
        return sb.toString();
    }
}
