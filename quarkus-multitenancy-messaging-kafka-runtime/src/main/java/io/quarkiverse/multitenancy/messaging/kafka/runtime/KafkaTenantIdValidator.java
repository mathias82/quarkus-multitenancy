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
package io.quarkiverse.multitenancy.messaging.kafka.runtime;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.messaging.kafka.runtime.config.KafkaTenantConfig;

/**
 * Validates tenant identifiers crossing the Kafka trust boundary.
 */
@ApplicationScoped
public class KafkaTenantIdValidator implements KafkaTenantValidator {

    private static final int LOG_PREVIEW_MAX_LENGTH = 64;
    private static final char SANITIZED_CONTROL_CHAR = '�';

    private final boolean enabled;
    private final int maxLength;
    private final String patternSource;
    private final Pattern pattern;

    @Inject
    public KafkaTenantIdValidator(KafkaTenantConfig config) {
        if (config.headerName().isBlank()) {
            throw new IllegalStateException(
                    "quarkus.multi-tenant.messaging.kafka.header-name must not be blank");
        }

        KafkaTenantConfig.TenantIdConfig tenantId = config.tenantId();
        if (tenantId.maxLength() < 1) {
            throw new IllegalStateException(
                    "quarkus.multi-tenant.messaging.kafka.tenant-id.max-length must be greater than zero");
        }

        this.enabled = tenantId.validationEnabled();
        this.maxLength = tenantId.maxLength();
        this.patternSource = tenantId.pattern();
        try {
            this.pattern = Pattern.compile(tenantId.pattern());
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException(
                    "Invalid quarkus.multi-tenant.messaging.kafka.tenant-id.pattern: '"
                            + tenantId.pattern() + "'",
                    e);
        }
    }

    /**
     * Checks a non-null tenant identifier against the configured policy.
     * Blank identifiers are always rejected, including when optional pattern
     * validation is disabled.
     *
     * @param tenantId tenant identifier read from or written to Kafka
     * @return a log-safe rejection reason, or empty when accepted
     */
    @Override
    public Optional<String> validate(String tenantId) {
        if (tenantId.isBlank()) {
            return Optional.of("is blank");
        }
        if (!enabled) {
            return Optional.empty();
        }
        if (tenantId.length() > maxLength) {
            return Optional.of(String.format(
                    "length %d exceeds the configured maximum of %d (sanitized: '%s')",
                    tenantId.length(), maxLength, sanitizeForLog(tenantId)));
        }
        if (!pattern.matcher(tenantId).matches()) {
            return Optional.of(String.format(
                    "does not match the configured pattern '%s' (sanitized: '%s')",
                    patternSource, sanitizeForLog(tenantId)));
        }
        return Optional.empty();
    }

    static String sanitizeForLog(String raw) {
        int limit = Math.min(raw.length(), LOG_PREVIEW_MAX_LENGTH);
        StringBuilder result = new StringBuilder(limit + 1);
        for (int i = 0; i < limit; i++) {
            char c = raw.charAt(i);
            result.append(Character.isISOControl(c) ? SANITIZED_CONTROL_CHAR : c);
        }
        if (raw.length() > LOG_PREVIEW_MAX_LENGTH) {
            result.append('…');
        }
        return result.toString();
    }
}
