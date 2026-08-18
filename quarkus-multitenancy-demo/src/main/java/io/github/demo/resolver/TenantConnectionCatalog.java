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

import java.util.Optional;
import java.util.Set;

/**
 * Application-owned source of tenant database connection details.
 * <p>
 * The demo uses configuration as the backing store. A real application can
 * replace it with a database, service, or secrets-aware catalog.
 */
public interface TenantConnectionCatalog {

    Optional<TenantDatabase> find(String tenantId);

    Set<String> tenantIds();
}
