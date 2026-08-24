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

import io.quarkus.test.junit.QuarkusTestProfile;

public class ProgrammaticH2Profile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
                Map.entry("quarkus.datasource.__bootstrap.db-kind", "h2"),
                Map.entry("quarkus.datasource.__bootstrap.jdbc.url", h2Url("bootstrap", "h2-bootstrap.sql")),
                Map.entry("quarkus.datasource.__bootstrap.username", "sa"),
                Map.entry("quarkus.datasource.__bootstrap.password", "sa"),
                Map.entry("quarkus.hibernate-orm.dialect", "org.hibernate.dialect.H2Dialect"),
                Map.entry("quarkus.hibernate-orm.schema-management.strategy", "none"),
                Map.entry("quarkus.hibernate-orm.log.sql", "false"),
                Map.entry("demo.tenant-catalog.tenants.tenant1.jdbc-url", h2Url("tenant1", "h2-tenant1.sql")),
                Map.entry("demo.tenant-catalog.tenants.tenant1.username", "sa"),
                Map.entry("demo.tenant-catalog.tenants.tenant1.password", "sa"),
                Map.entry("demo.tenant-catalog.tenants.tenant2.jdbc-url", h2Url("tenant2", "h2-tenant2.sql")),
                Map.entry("demo.tenant-catalog.tenants.tenant2.username", "sa"),
                Map.entry("demo.tenant-catalog.tenants.tenant2.password", "sa"));
    }

    private static String h2Url(String database, String script) {
        return "jdbc:h2:mem:" + database
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
                + ";INIT=RUNSCRIPT FROM 'classpath:db/" + script + "'";
    }
}
