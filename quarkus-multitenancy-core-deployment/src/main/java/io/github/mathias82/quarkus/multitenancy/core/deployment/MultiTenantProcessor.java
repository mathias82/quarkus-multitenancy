package io.github.mathias82.quarkus.multitenancy.core.deployment;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.DefaultTenantContext;
import io.github.mathias82.quarkus.multitenancy.core.runtime.core.CompositeTenantResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class MultiTenantProcessor {

    private static final String FEATURE = "quarkus-multitenancy-core";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerCoreBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(DefaultTenantContext.class)
                .addBeanClass(CompositeTenantResolver.class)
                .build());
    }
}
