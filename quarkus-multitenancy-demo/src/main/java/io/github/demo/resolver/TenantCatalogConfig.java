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
import java.util.Map;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "demo.tenant-catalog")
public interface TenantCatalogConfig {

    Map<String, Tenant> tenants();

    interface Tenant {

        String jdbcUrl();

        String username();

        String password();

        @WithDefault("0")
        int initialSize();

        @WithDefault("0")
        int minSize();

        @WithDefault("10")
        int maxSize();

        @WithDefault("30S")
        Duration acquisitionTimeout();

        @WithDefault("5M")
        Duration maxLifetime();
    }
}
