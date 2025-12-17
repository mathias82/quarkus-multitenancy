package io.github.mathias82.quarkus.multitenancy.http.deployment;

import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import io.github.mathias82.quarkus.multitenancy.http.runtime.filter.TenantFilter;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConfigClassBuildItem;

import java.util.Set;

public class MultiTenancyHttpProcessor {

    @BuildStep
    ConfigClassBuildItem registerHttpConfig() {
        return new ConfigClassBuildItem(
                HttpTenantConfig.class,
                Set.of(),                  // configComponentInterfaces
                Set.of(),                  // types
                Set.of(),                  // generatedClasses
                "quarkus.multi-tenant.http",
                ConfigClassBuildItem.Kind.MAPPING
        );
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(TenantFilter.class)
                .build();
    }
}
