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

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Replays {@link AsyncCancellationTest} against the native binary. The cancellation is driven by a
 * real client abort and read back over HTTP, so it runs black-box in native the same way it does in
 * the JVM.
 */
@QuarkusIntegrationTest
class AsyncCancellationIT extends AsyncCancellationTest {
}
