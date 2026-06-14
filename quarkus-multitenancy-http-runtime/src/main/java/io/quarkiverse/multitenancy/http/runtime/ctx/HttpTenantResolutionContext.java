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
package io.quarkiverse.multitenancy.http.runtime.ctx;

import java.util.Optional;

import jakarta.ws.rs.container.ContainerRequestContext;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;

public class HttpTenantResolutionContext implements TenantResolutionContext {

    private final ContainerRequestContext requestContext;

    public HttpTenantResolutionContext(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public <T> Optional<T> get(Class<T> type) {
        if (type.isInstance(requestContext)) {
            return Optional.of(type.cast(requestContext));
        }

        if (type.isAssignableFrom(requestContext.getHeaders().getClass())) {
            return Optional.of(type.cast(requestContext.getHeaders()));
        }

        return Optional.empty();
    }
}
