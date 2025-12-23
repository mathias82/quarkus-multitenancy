package io.github.mathias82.quarkus.multitenancy.http.deployment;

import io.github.mathias82.quarkus.multitenancy.http.runtime.filter.TenantFilter;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.CookieTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.HeaderTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.JwtTenantResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class MultiTenantHttpProcessor {

    private static final String FEATURE = "quarkus-multitenancy-http";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerHttpBeans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(HeaderTenantResolver.class)
                .addBeanClass(TenantFilter.class)
                .addBeanClass(HeaderTenantResolver.class)
                .addBeanClass(JwtTenantResolver.class)
                .addBeanClass(CookieTenantResolver.class)
                .build());
    }
}
