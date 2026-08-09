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
 * Replays {@link AsyncPropagationTest} against the native binary with {@code mvn verify
 * -Dnative}. This is the native half of acceptance criterion "JVM and native integration tests
 * cover the supported propagation contract" (issue #66), guarding against native-image
 * regressions in reactive and asynchronous tenant propagation.
 */
@QuarkusIntegrationTest
class AsyncPropagationIT extends AsyncPropagationTest {
}
