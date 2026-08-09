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

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Verifies the cancellation half of issue #66 criterion 7: a reactive request that is cancelled
 * (here by the client aborting mid-flight) still runs its cancellation callback with the tenant
 * bound, and leaves no tenant behind for a later request. The tenant seen at cancellation is read
 * back over HTTP, so {@link AsyncCancellationIT} can replay this against the native binary.
 */
@QuarkusTest
class AsyncCancellationTest {

    @TestHTTPResource("/async/uni-cancel")
    URL cancelUrl;

    @Test
    void cancelledReactiveRequestRunsWithTenantBoundAndLeavesNoLeak() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(cancelUrl.toURI())
                .header("X-Tenant", "acme")
                .timeout(Duration.ofMillis(500))
                .GET()
                .build();

        // The endpoint only emits after 30s, so the 500ms client timeout aborts the request and
        // the server cancels the subscription.
        assertThrows(HttpTimeoutException.class, () -> client.send(request, BodyHandlers.ofString()));

        // The cancellation callback observed the tenant that was bound to the cancelled request.
        when()
                .get("/async/uni-cancel/observed")
                .then()
                .statusCode(200)
                .body(is("acme"));

        // The cancelled request left no tenant behind: a following no-header request falls back to
        // the default rather than inheriting "acme".
        when()
                .get("/async/uni-emiton")
                .then()
                .statusCode(200)
                .body(is("public"));
    }
}
