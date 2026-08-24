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
package io.quarkiverse.multitenancy.orm.runtime.config;

import java.util.Optional;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

/**
 * Build-time configuration for integrating the shared tenant context with
 * Hibernate ORM persistence units.
 */
@ConfigMapping(prefix = "quarkus.multi-tenant.orm")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface OrmTenantBuildTimeConfig {

    /**
     * Named Hibernate ORM persistence units that should obtain their tenant
     * identifier from the shared {@code TenantContext}.
     *
     * <p>
     * The default persistence unit is always integrated for backward
     * compatibility. Every configured named persistence unit must exist and have
     * Hibernate ORM multitenancy enabled.
     */
    Optional<Set<String>> namedPersistenceUnits();
}
