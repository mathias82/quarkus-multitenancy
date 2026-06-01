package io.quarkiverse.multitenancy.http.deployment;

import io.quarkiverse.multitenancy.http.runtime.config.HttpTenantConfig;
import io.quarkiverse.multitenancy.http.runtime.filter.TenantFilter;
import io.quarkiverse.multitenancy.http.runtime.resolver.CookieTenantResolver;
import io.quarkiverse.multitenancy.http.runtime.resolver.HeaderTenantResolver;
import io.quarkiverse.multitenancy.http.runtime.resolver.JwtTenantResolver;
import io.quarkiverse.multitenancy.http.runtime.resolver.PathTenantResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConfigMappingBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class MultiTenantHttpProcessor {

    private static final String FEATURE = "quarkus-multitenancy-http";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ConfigMappingBuildItem httpTenantConfig() {
        return new ConfigMappingBuildItem(HttpTenantConfig.class, "quarkus.multi-tenant.http");
    }

    @BuildStep
    void registerHttpBeans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(TenantFilter.class)
                .addBeanClass(HeaderTenantResolver.class)
                .addBeanClass(JwtTenantResolver.class)
                .addBeanClass(CookieTenantResolver.class)
                .addBeanClass(PathTenantResolver.class)
                .build());
    }
}
