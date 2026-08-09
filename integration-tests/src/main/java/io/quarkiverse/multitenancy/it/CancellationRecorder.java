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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Captures the tenant observed inside the cancellation callback of {@code /async/uni-cancel}. The
 * captured value is exposed over HTTP (rather than injected) so {@link AsyncCancellationTest} works
 * unchanged black-box against the native binary as well (issue #66, criterion 7).
 */
@ApplicationScoped
public class CancellationRecorder {

    private final CompletableFuture<String> tenantAtCancellation = new CompletableFuture<>();

    void recordCancellation(String tenantId) {
        tenantAtCancellation.complete(tenantId);
    }

    /**
     * Completes with the tenant seen at cancellation, or times out so the observing request never
     * hangs forever if the cancellation callback never runs.
     */
    CompletionStage<String> observed() {
        return tenantAtCancellation.copy().orTimeout(15, TimeUnit.SECONDS);
    }
}
