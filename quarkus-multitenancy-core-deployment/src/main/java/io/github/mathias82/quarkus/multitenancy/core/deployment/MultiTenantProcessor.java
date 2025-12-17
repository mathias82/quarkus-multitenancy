package io.github.mathias82.quarkus.multitenancy.core.deployment;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.DefaultTenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.TenantResolverProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;

public class MultiTenantProcessor {

    @BuildStep
    AdditionalBeanBuildItem registerCoreBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(DefaultTenantContext.class)
                .addBeanClass(TenantResolverProducer.class)
                .build();
    }
}
