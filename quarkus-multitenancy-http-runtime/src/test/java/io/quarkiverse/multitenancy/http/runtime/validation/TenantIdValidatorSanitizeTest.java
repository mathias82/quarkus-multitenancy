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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit coverage for {@link TenantIdValidator#sanitizeForLog(String)}, the
 * log-injection guard. It runs without booting Quarkus.
 */
class TenantIdValidatorSanitizeTest {

    @Test
    void stripsCarriageReturnAndLineFeed() {
        String sanitized = TenantIdValidator.sanitizeForLog("acme\r\nGET /admin HTTP/1.1");

        assertFalse(sanitized.contains("\r"), "carriage return must not survive");
        assertFalse(sanitized.contains("\n"), "line feed must not survive");
    }

    @Test
    void leavesACleanIdentifierUntouched() {
        assertEquals("acme-prod_1", TenantIdValidator.sanitizeForLog("acme-prod_1"));
    }

    @Test
    void truncatesAnOverlongValue() {
        String sanitized = TenantIdValidator.sanitizeForLog("a".repeat(200));

        assertTrue(sanitized.endsWith("…"), "truncated value must carry the ellipsis marker");
        assertTrue(sanitized.length() <= 65, "preview must stay bounded (64 chars + ellipsis)");
    }
}
