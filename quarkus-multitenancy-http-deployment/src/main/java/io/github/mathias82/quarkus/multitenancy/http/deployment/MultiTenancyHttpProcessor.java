package io.github.mathias82.quarkus.multitenancy.http.deployment;

import io.github.mathias82.quarkus.multitenancy.http.runtime.config.HttpTenantConfig;
import io.github.mathias82.quarkus.multitenancy.http.runtime.filter.TenantFilter;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.CookieTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.HeaderTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.JwtClaimTenantResolver;
import io.github.mathias82.quarkus.multitenancy.http.runtime.resolver.PathTenantResolver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;

/**
 * Deployment processor for the HTTP multitenancy extension.
 *
 * Ensures all runtime beans are registered and unremovable so that
 * CDI can inject them into the running Quarkus application.
 */
public class MultiTenancyHttpProcessor {

    @BuildStep
    AdditionalBeanBuildItem registerHttpBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(TenantFilter.class)
                .addBeanClass(HeaderTenantResolver.class)
                .addBeanClass(CookieTenantResolver.class)
                .addBeanClass(JwtClaimTenantResolver.class)
                .addBeanClass(PathTenantResolver.class)
                .addBeanClass(HttpTenantConfig.class)
                .build();
    }
}
