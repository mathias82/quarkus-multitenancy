package io.github.demo.resource.config;

import io.github.demo.resource.utils.DatabaseTestUtils;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class PostgresCheckProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {

        boolean tenant1Up = DatabaseTestUtils.isPostgresUp("localhost", 5433);
        boolean tenant2Up = DatabaseTestUtils.isPostgresUp("localhost", 5434);

        if (!tenant1Up || !tenant2Up) {
            System.err.println("PostgreSQL not available. Disabling ORM and Datasource.");
            return Map.of(
                    "quarkus.hibernate-orm.enabled", "false",
                    "quarkus.datasource.enabled", "false"
            );
        }

        return Map.of(
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.datasource.jdbc.url", "jdbc:postgresql://localhost:5435/testdb",
                "quarkus.datasource.username", "test",
                "quarkus.datasource.password", "test",
                "quarkus.hibernate-orm.database.generation", "drop-and-create"
        );
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }
}
