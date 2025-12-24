package io.github.mathias82.quarkus.multitenancy.orm.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

/**
 * Registers the ORM multitenancy extension as a Quarkus feature.
 */
public class OrmTenantProcessor {

    private static final String FEATURE = "quarkus-multitenancy-orm";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
