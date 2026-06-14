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
package io.github.demo.resource.config;

import java.util.Map;

import io.github.demo.resource.utils.DatabaseTestUtils;
import io.quarkus.test.junit.QuarkusTestProfile;

public class PostgresCheckProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {

        boolean tenant1Up = DatabaseTestUtils.isPostgresUp("localhost", 5433);
        boolean tenant2Up = DatabaseTestUtils.isPostgresUp("localhost", 5434);

        if (!tenant1Up || !tenant2Up) {
            System.err.println("PostgreSQL not available. Disabling ORM and Datasource.");
            return Map.of(
                    "quarkus.hibernate-orm.enabled", "false",
                    "quarkus.datasource.enabled", "false");
        }

        return Map.of(
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.hibernate-orm.database.generation", "drop-and-create");
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }
}
