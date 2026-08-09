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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 * Every boundary here is exercised in a plain Quarkus REST application with no added dependency,
 * including the MicroProfile {@code ManagedExecutor} case, because {@code quarkus-rest} already
 * brings {@code quarkus-smallrye-context-propagation} transitively.
 *
 * <p>
 * The synchronous-only behaviour of {@code TenantContextRunner} with deferred results
 * ({@code Uni} / {@code CompletionStage}), including rejection before a deferred body runs, is
 * covered as a unit contract in {@code TenantContextRunnerTest} and is not duplicated here.
 * Cancellation is covered by {@link AsyncCancellationTest}.
 */
@QuarkusTest
class AsyncPropagationTest {

    private static final int CLIENT_THREADS = 32;

    // ---- Supported boundaries: a tenant resolved from the request survives each one. ----

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

    // ---- Unsupported boundary fails predictably instead of returning a wrong tenant. ----

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

    // ---- Isolation: concurrent requests with a UNIQUE tenant each stay isolated. ----
    //
    // A unique tenant per request means any cross-request leak surfaces two ways: a body that
    // does not match its own tenant, and a duplicate value in the observed set. Repeating a small
    // set of tenant values would hide a leak between two requests that happen to share a value.

    @Test
    void concurrentIsolationAcrossUniEmitOn() throws Exception {
        assertConcurrentIsolation("/async/uni-emiton", 200);
    }

    @Test
    void concurrentIsolationAcrossBlockingWorkerThreads() throws Exception {
        assertConcurrentIsolation("/async/blocking", 150);
    }

    @Test
    void concurrentIsolationAcrossContextAwareCompletionStage() throws Exception {
        assertConcurrentIsolation("/async/cs-managed", 150);
    }

    // ---- No leak: no-header requests must never observe a tenant, even while tenant traffic
    // is hammering the same worker pool concurrently. A single sequential probe would miss a
    // leak stuck on any other worker thread, so probes are concurrent and interleaved. ----

    @Test
    void noHeaderRequestsNeverObserveATenantUnderConcurrentTenantTraffic() throws Exception {
        int tenantRequests = 200;
        int probeRequests = 96;
        ExecutorService pool = Executors.newFixedThreadPool(CLIENT_THREADS);
        try {
            List<Future<?>> traffic = new ArrayList<>(tenantRequests);
            List<Future<String>> probes = new ArrayList<>(probeRequests);
            int max = Math.max(tenantRequests, probeRequests);
            for (int i = 0; i < max; i++) {
                if (i < tenantRequests) {
                    String tenant = "t" + i;
                    traffic.add(pool.submit(() -> given()
                            .header("X-Tenant", tenant)
                            .when()
                            .get("/async/uni-emiton")
                            .then()
                            .statusCode(200)
                            .body(is(tenant))));
                }
                if (i < probeRequests) {
                    probes.add(pool.submit(() -> when()
                            .get("/async/uni-emiton")
                            .then()
                            .statusCode(200)
                            .extract()
                            .asString()));
                }
            }

            for (Future<String> probe : probes) {
                assertEquals("public", probe.get(),
                        "a no-header request observed a tenant while tenant traffic was running");
            }
            drain(traffic);
        } finally {
            pool.shutdownNow();
        }
    }

    // ---- The raw (non-context-aware) pool must never retain a tenant across its reused threads.
    // With only four pool threads, hammering it concurrently forces heavy reuse. ----

    @Test
    void rawExecutorNeverRetainsATenantAcrossReusedThreads() throws Exception {
        int requests = 60;
        ExecutorService pool = Executors.newFixedThreadPool(CLIENT_THREADS);
        try {
            List<Future<?>> futures = new ArrayList<>(requests);
            for (int i = 0; i < requests; i++) {
                String tenant = "t" + i;
                futures.add(pool.submit(() -> given()
                        .header("X-Tenant", tenant)
                        .when()
                        .get("/async/raw-executor")
                        .then()
                        .statusCode(200)
                        .body(is(AsyncTenantResource.UNSUPPORTED_PREFIX + "ContextNotActiveException"))));
            }
            drain(futures);
        } finally {
            pool.shutdownNow();
        }
    }

    // ---- A terminally failed async request (500) must not pollute later requests. Failures and
    // no-header probes are interleaved so probes run while worker threads are dirty from failures.

    @Test
    void terminalAsyncFailureDoesNotPolluteLaterRequests() throws Exception {
        int failures = 120;
        int probeRequests = 96;
        ExecutorService pool = Executors.newFixedThreadPool(CLIENT_THREADS);
        try {
            List<Future<?>> fails = new ArrayList<>(failures);
            List<Future<String>> probes = new ArrayList<>(probeRequests);
            int max = Math.max(failures, probeRequests);
            for (int i = 0; i < max; i++) {
                if (i < failures) {
                    String tenant = "t" + i;
                    fails.add(pool.submit(() -> given()
                            .header("X-Tenant", tenant)
                            .when()
                            .get("/async/uni-fail-terminal")
                            .then()
                            .statusCode(500)));
                }
                if (i < probeRequests) {
                    probes.add(pool.submit(() -> when()
                            .get("/async/uni-emiton")
                            .then()
                            .statusCode(200)
                            .extract()
                            .asString()));
                }
            }

            for (Future<String> probe : probes) {
                assertEquals("public", probe.get(),
                        "a no-header request observed a tenant after a terminal async failure");
            }
            drain(fails);
        } finally {
            pool.shutdownNow();
        }
    }

    private void assertConcurrentIsolation(String path, int total) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(CLIENT_THREADS);
        try {
            List<Future<String[]>> futures = new ArrayList<>(total);
            for (int i = 0; i < total; i++) {
                String tenant = "t" + i;
                futures.add(pool.submit(() -> {
                    String body = given()
                            .header("X-Tenant", tenant)
                            .when()
                            .get(path)
                            .then()
                            .statusCode(200)
                            .extract()
                            .asString();
                    return new String[] { tenant, body };
                }));
            }

            Set<String> observed = ConcurrentHashMap.newKeySet();
            for (Future<String[]> future : futures) {
                String[] result = future.get();
                assertEquals(result[0], result[1],
                        "request for tenant '" + result[0] + "' on " + path + " observed tenant '"
                                + result[1] + "' across the async boundary");
                observed.add(result[1]);
            }
            assertEquals(total, observed.size(),
                    "every concurrent request on " + path + " must observe its own unique tenant");
        } finally {
            pool.shutdownNow();
        }
    }

    private static void drain(List<Future<?>> futures) throws Exception {
        for (Future<?> future : futures) {
            future.get();
        }
    }
}
