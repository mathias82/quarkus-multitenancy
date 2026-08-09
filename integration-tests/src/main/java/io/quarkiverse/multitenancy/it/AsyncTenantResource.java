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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.context.ManagedExecutor;

import io.quarkiverse.multitenancy.core.runtime.context.TenantContext;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Reads the tenant bound by the HTTP filter <em>after</em> crossing an asynchronous boundary and
 * echoes what it sees, so the integration suite can pin down the tenant-context propagation
 * contract for reactive and asynchronous execution (issue #66).
 *
 * <p>
 * All of these boundaries work in a plain Quarkus REST application with no added dependency:
 * {@code quarkus-rest} already brings {@code quarkus-smallrye-context-propagation} transitively, so
 * the MicroProfile {@code ManagedExecutor} used by {@code /async/cs-managed} is available too.
 *
 * <p>
 * Each endpoint exercises one boundary:
 * <ul>
 * <li>{@code /async/uni-emiton} — a Mutiny {@code Uni} whose work is emitted on the Quarkus
 * worker pool. <strong>Supported:</strong> the duplicated context travels with it.</li>
 * <li>{@code /async/blocking} — a {@code @Blocking} endpoint that reads on a worker thread.
 * <strong>Supported.</strong></li>
 * <li>{@code /async/cs-managed} — a {@code CompletionStage} run on a MicroProfile
 * {@link ManagedExecutor}. <strong>Supported.</strong></li>
 * <li>{@code /async/raw-executor} — a {@code CompletionStage} run on a raw, non-context-aware
 * JDK pool. <strong>Unsupported:</strong> reading the request-scoped context there throws
 * {@link ContextNotActiveException}, which this endpoint reports as a stable marker.</li>
 * <li>{@code /async/uni-fail} — a failing {@code Uni} whose recovery stage reads the tenant,
 * proving the context is still bound along the failure path.</li>
 * </ul>
 */
@Path("/async")
public class AsyncTenantResource {

    static final String UNSUPPORTED_PREFIX = "unsupported:";

    @Inject
    TenantContext tenantContext;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    RawExecutor rawExecutor;

    @Inject
    CancellationRecorder cancellationRecorder;

    private String currentTenant() {
        return tenantContext.getTenantId().orElse("<none>");
    }

    @GET
    @Path("uni-emiton")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> uniEmitOn() {
        return Uni.createFrom().item("start")
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(ignored -> currentTenant());
    }

    @GET
    @Path("blocking")
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    public String blocking() {
        return currentTenant();
    }

    @GET
    @Path("cs-managed")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> completionStageManaged() {
        return managedExecutor.supplyAsync(this::currentTenant);
    }

    @GET
    @Path("raw-executor")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> rawExecutorStage() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return currentTenant();
            } catch (ContextNotActiveException e) {
                return UNSUPPORTED_PREFIX + e.getClass().getSimpleName();
            }
        }, rawExecutor.get());
    }

    @GET
    @Path("uni-fail")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> uniFail() {
        return Uni.createFrom().<String> failure(new IllegalStateException("boom"))
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().recoverWithItem(() -> "recovered:" + currentTenant());
    }

    /**
     * A {@code Uni} that fails on the worker pool without recovering, so the request terminates
     * with a 500. Used to prove that a terminally failed async request tears its tenant context
     * down and does not pollute a later request (issue #66, criterion 7).
     */
    @GET
    @Path("uni-fail-terminal")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> uniFailTerminal() {
        return Uni.createFrom().item("start")
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(ignored -> {
                    throw new IllegalStateException("boom:" + currentTenant());
                });
    }

    /**
     * A {@code Uni} that only emits after a long delay, so a client that aborts the request
     * causes the server to cancel the subscription. The cancellation callback reads the tenant
     * and hands it to {@link CancellationRecorder}, proving the tenant is still bound along the
     * cancellation path (issue #66, criterion 7).
     */
    @GET
    @Path("uni-cancel")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> uniCancel() {
        return Uni.createFrom().item("start")
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().delayIt().by(Duration.ofSeconds(30))
                .onItem().transform(ignored -> currentTenant())
                .onCancellation().invoke(() -> cancellationRecorder.recordCancellation(currentTenant()));
    }

    /**
     * Exposes the tenant that {@code /async/uni-cancel} saw in its cancellation callback, so the
     * test can assert it over HTTP and stay black-box in native mode.
     */
    @GET
    @Path("uni-cancel/observed")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> uniCancelObserved() {
        return cancellationRecorder.observed();
    }
}
