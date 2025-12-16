package io.github.mathias82.quarkus.multitenancy.http.deployment;

import io.github.mathias82.quarkus.multitenancy.http.runtime.filter.TenantFilter;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;

public class MultiTenancyHttpProcessor {

    @BuildStep
    AdditionalBeanBuildItem registerHttpBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(TenantFilter.class)
                .build();
    }
}
