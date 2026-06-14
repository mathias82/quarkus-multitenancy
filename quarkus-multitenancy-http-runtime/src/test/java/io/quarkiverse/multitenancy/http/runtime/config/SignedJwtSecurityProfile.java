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
package io.quarkiverse.multitenancy.http.runtime.config;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Wires SmallRye JWT to verify tokens with the committed test public key. The
 * matching private key is used by tests to mint tokens via
 * {@code io.smallrye.jwt.build.Jwt}.
 */
public class SignedJwtSecurityProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                // jwt is opt-in since the default chain is header,cookie — enable it for these tests.
                "quarkus.multi-tenant.http.strategy", "header,jwt,cookie",
                "mp.jwt.verify.publickey.location", "test-public-key.pem",
                "mp.jwt.verify.publickey.algorithm", "RS256",
                "mp.jwt.verify.issuer", "https://multitenancy.test/issuer",
                "smallrye.jwt.sign.key.location", "test-private-key.pem");
    }
}
