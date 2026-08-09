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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * A plain JDK executor that is deliberately <strong>not</strong> context-aware, used to
 * demonstrate the unsupported side of the async propagation contract (issue #66): work
 * offloaded to a raw thread pool loses the Vert.x duplicated context, so the
 * {@code @RequestScoped} tenant context is no longer active there.
 *
 * <p>
 * The pool is created in {@link PostConstruct} (runtime) rather than a field initializer so the
 * native image never captures a live thread pool at build time.
 */
@ApplicationScoped
public class RawExecutor {

    private ExecutorService executor;

    @PostConstruct
    void start() {
        executor = Executors.newFixedThreadPool(4);
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    ExecutorService get() {
        return executor;
    }
}
