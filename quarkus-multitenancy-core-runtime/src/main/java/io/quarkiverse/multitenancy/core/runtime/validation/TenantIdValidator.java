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
package io.quarkiverse.multitenancy.core.runtime.validation;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.quarkiverse.multitenancy.core.runtime.context.ReservedTenantIds;

/**
 * Shared tenant-id validation policy used by transport-specific resolution paths.
 */
public class TenantIdValidator {

    private static final int LOG_PREVIEW_MAX_LENGTH = 64;
    private static final char SANITIZED_CONTROL_CHAR = '�';

    private final boolean enabled;
    private final int maxLength;
    private final String patternSource;
    private final Pattern pattern;

    protected TenantIdValidator() {
        this(true, 64, "[A-Za-z0-9_-]+", "tenant-id.pattern");
    }

    public TenantIdValidator(boolean enabled, int maxLength, String patternSource, String patternPropertyName) {
        this.enabled = enabled;
        this.maxLength = maxLength;
        this.patternSource = patternSource;
        try {
            this.pattern = Pattern.compile(patternSource);
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException("Invalid " + patternPropertyName + ": '" + patternSource + "'", e);
        }
    }

    public Optional<String> validate(String tenantId) {
        if (ReservedTenantIds.isReserved(tenantId)) {
            return Optional.of("is reserved for internal use");
        }
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
