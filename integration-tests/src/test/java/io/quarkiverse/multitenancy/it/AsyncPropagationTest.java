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
package io.quarkiverse.multitenancy.it;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Pins down the tenant-context propagation contract for reactive and asynchronous execution
 * (issue #66) end to end against the running application. This class runs in the JVM (surefire);
 * the {@link AsyncPropagationIT} subclass replays the same assertions against the native binary,
 * so the supported contract is verified identically in JVM and native mode.
 *
 * <p>
 * The synchronous-only behaviour of {@code TenantContextRunner} with deferred results
 * ({@code Uni} / {@code CompletionStage}) is covered as a unit contract in
 * {@code TenantContextRunnerTest} and is not duplicated here.
 */
@QuarkusTest
class AsyncPropagationTest {

    // A tenant resolved from the request must survive each supported async boundary.

    @Test
    void tenantSurvivesUniEmitOnWorkerPool() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/async/uni-emiton")
                .then()
                .statusCode(200)
                .body(is("acme"));
    }

    @Test
    void tenantSurvivesBlockingWorkerThread() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/async/blocking")
                .then()
                .statusCode(200)
                .body(is("acme"));
    }

    @Test
    void tenantSurvivesContextAwareCompletionStage() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/async/cs-managed")
                .then()
                .statusCode(200)
                .body(is("acme"));
    }

    // The unsupported boundary fails predictably rather than silently returning a wrong tenant.

    @Test
    void rawExecutorHasNoActiveTenantContext() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/async/raw-executor")
                .then()
                .statusCode(200)
                .body(is(AsyncTenantResource.UNSUPPORTED_PREFIX + "ContextNotActiveException"));
    }

    // The failure path keeps the tenant bound while the pipeline is still live.

    @Test
    void tenantSurvivesUniFailureRecovery() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/async/uni-fail")
                .then()
                .statusCode(200)
                .body(is("recovered:acme"));
    }

    // Concurrent requests carrying different tenants stay isolated across the async boundary:
    // no request ever observes another request's tenant.

    @Test
    void concurrentRequestsWithDifferentTenantsStayIsolated() throws Exception {
        int distinctTenants = 5;
        int requestsPerTenant = 20;
        int total = distinctTenants * requestsPerTenant;

        List<String> expected = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            expected.add("tenant" + (i % distinctTenants));
        }

        ExecutorService pool = Executors.newFixedThreadPool(16);
        try {
            List<Future<Map.Entry<String, String>>> futures = new ArrayList<>(total);
            for (String tenant : expected) {
                Callable<Map.Entry<String, String>> call = () -> {
                    String body = given()
                            .header("X-Tenant", tenant)
                            .when()
                            .get("/async/uni-emiton")
                            .then()
                            .statusCode(200)
                            .extract()
                            .asString();
                    return Map.entry(tenant, body);
                };
                futures.add(pool.submit(call));
            }

            for (Future<Map.Entry<String, String>> future : futures) {
                Map.Entry<String, String> result = future.get();
                assertEquals(result.getKey(), result.getValue(),
                        "request for tenant '" + result.getKey() + "' observed tenant '" + result.getValue()
                                + "' across the async boundary");
            }
        } finally {
            pool.shutdownNow();
        }
    }

    // No tenant leaks onto a reused worker thread: after tenant traffic, a request with no tenant
    // falls back to the default instead of inheriting a previous request's tenant.

    @Test
    void noTenantLeaksToASubsequentRequestOnAReusedWorkerThread() {
        for (int i = 0; i < 20; i++) {
            given()
                    .header("X-Tenant", "acme")
                    .when()
                    .get("/async/uni-emiton")
                    .then()
                    .statusCode(200)
                    .body(is("acme"));
        }

        when()
                .get("/async/uni-emiton")
                .then()
                .statusCode(200)
                .body(is("public"));
    }

    // A failed async request must not pollute a later clean request.

    @Test
    void failedAsyncRequestDoesNotPolluteALaterRequest() {
        given()
                .header("X-Tenant", "acme")
                .when()
                .get("/async/uni-fail")
                .then()
                .statusCode(200)
                .body(is("recovered:acme"));

        when()
                .get("/async/uni-emiton")
                .then()
                .statusCode(200)
                .body(is("public"));
    }
}
