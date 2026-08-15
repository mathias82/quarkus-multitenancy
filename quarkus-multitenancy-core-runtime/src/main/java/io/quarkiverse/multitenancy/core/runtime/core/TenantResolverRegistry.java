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
package io.quarkiverse.multitenancy.core.runtime.core;

import java.util.Comparator;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkus.arc.All;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.runtime.Startup;

/**
 * Provides every {@link TenantResolver} in a stable, documented order.
 *
 * <p>
 * Quarkus Arc exposes the priority assigned through {@code @Priority} on the
 * bean metadata. Higher priorities run first. The implementation class name is
 * used as a stable tie-breaker, avoiding any dependency on CDI discovery order.
 */
@Startup
@ApplicationScoped
public class TenantResolverRegistry {

    private static final Comparator<InstanceHandle<TenantResolver>> RESOLVER_ORDER = Comparator
            .<InstanceHandle<TenantResolver>> comparingInt(handle -> handle.getBean().getPriority())
            .reversed()
            .thenComparing(TenantResolverRegistry::implementationClassName);

    @Inject
    @All
    List<InstanceHandle<TenantResolver>> resolverHandles;

    private List<InstanceHandle<TenantResolver>> orderedHandles;

    @PostConstruct
    void initialize() {
        orderedHandles = resolverHandles.stream()
                .sorted(RESOLVER_ORDER)
                .toList();
        validateDeterministicTieBreakers(orderedHandles.stream()
                .map(TenantResolverRegistry::descriptor)
                .toList());
    }

    /**
     * Returns a snapshot of all resolvers ordered by descending priority and
     * then by implementation class name.
     *
     * @return immutable ordered resolver list
     */
    public List<TenantResolver> orderedResolvers() {
        return orderedHandles.stream()
                .map(InstanceHandle::get)
                .toList();
    }

    static void validateDeterministicTieBreakers(List<ResolverDescriptor> descriptors) {
        for (int i = 1; i < descriptors.size(); i++) {
            ResolverDescriptor previous = descriptors.get(i - 1);
            ResolverDescriptor current = descriptors.get(i);
            if (previous.priority() == current.priority()
                    && previous.implementationClassName().equals(current.implementationClassName())) {
                throw new IllegalStateException(String.format(
                        "Ambiguous TenantResolver order for implementation '%s' at priority %d. "
                                + "Assign distinct @Priority values to beans '%s' and '%s'.",
                        current.implementationClassName(),
                        current.priority(),
                        previous.beanIdentifier(),
                        current.beanIdentifier()));
            }
        }
    }

    private static ResolverDescriptor descriptor(InstanceHandle<TenantResolver> handle) {
        return new ResolverDescriptor(
                handle.getBean().getPriority(),
                implementationClassName(handle),
                handle.getBean().getIdentifier());
    }

    private static String implementationClassName(InstanceHandle<TenantResolver> handle) {
        Class<?> implementationClass = handle.getBean().getImplementationClass();
        if (implementationClass != null) {
            return implementationClass.getName();
        }
        return handle.getBean().getBeanClass().getName();
    }

    record ResolverDescriptor(int priority, String implementationClassName, String beanIdentifier) {
    }
}
