package io.github.mathias82.quarkus.multitenancy.core.deployment;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.DefaultTenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.CompositeTenantResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;

/**
 * Deployment processor for the Quarkus Multitenancy extension.
 *
 * Registers core runtime infrastructure beans.
 */
public class MultiTenantProcessor {

    /**
     * Register core runtime beans as unremovable.
     *
     * NOTE:
     * - No resolver implementations are registered here.
     * - Resolvers are discovered via CDI.
     */
    @BuildStep
    AdditionalBeanBuildItem registerCoreBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(DefaultTenantContext.class)
                .addBeanClass(CompositeTenantResolver.class)
                .build();
    }

    /**
     * Runtime initialization hook.
     */
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void init(MultiTenantRecorder recorder) {
        recorder.init();
    }
}
