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
package io.github.demo.resolver;

import java.time.Duration;
import java.util.Objects;

public record TenantDatabase(String jdbcUrl, String username, String password,
        int initialSize, int minSize, int maxSize,
        Duration acquisitionTimeout, Duration maxLifetime) {

    public TenantDatabase {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(acquisitionTimeout, "acquisitionTimeout");
        Objects.requireNonNull(maxLifetime, "maxLifetime");

        if (jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be blank");
        }
        if (username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (minSize < 0 || initialSize < minSize || maxSize < initialSize) {
            throw new IllegalArgumentException(
                    "Pool sizes must satisfy 0 <= minSize <= initialSize <= maxSize");
        }
        if (acquisitionTimeout.isNegative() || acquisitionTimeout.isZero()) {
            throw new IllegalArgumentException("acquisitionTimeout must be positive");
        }
        if (maxLifetime.isNegative() || maxLifetime.isZero()) {
            throw new IllegalArgumentException("maxLifetime must be positive");
        }
    }

    @Override
    public String toString() {
        return "TenantDatabase[jdbcUrl=" + jdbcUrl + ", username=" + username
                + ", password=<redacted>, initialSize=" + initialSize
                + ", minSize=" + minSize + ", maxSize=" + maxSize
                + ", acquisitionTimeout=" + acquisitionTimeout
                + ", maxLifetime=" + maxLifetime + "]";
    }
}
