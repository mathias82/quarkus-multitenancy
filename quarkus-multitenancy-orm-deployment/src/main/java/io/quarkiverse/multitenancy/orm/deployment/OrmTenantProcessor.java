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
package io.quarkiverse.multitenancy.orm.deployment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTransformation;
import org.jboss.jandex.DotName;

import io.quarkiverse.multitenancy.orm.runtime.adapter.OrmTenantResolverAdapter;
import io.quarkiverse.multitenancy.orm.runtime.config.OrmTenantBuildTimeConfig;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.deployment.HibernateOrmConfig;
import io.quarkus.hibernate.orm.deployment.HibernateOrmConfigPersistenceUnit;
import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;
import io.quarkus.runtime.configuration.ConfigurationException;

/**
 * Registers the ORM multitenancy extension as a Quarkus feature.
 */
public class OrmTenantProcessor {

    private static final String FEATURE = "quarkus-multitenancy-orm";
    private static final String NAMED_PERSISTENCE_UNITS_PROPERTY = "quarkus.multi-tenant.orm.named-persistence-units";
    private static final DotName ORM_TENANT_RESOLVER_ADAPTER = DotName.createSimple(OrmTenantResolverAdapter.class);
    private static final DotName PERSISTENCE_UNIT_EXTENSION = DotName.createSimple(PersistenceUnitExtension.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AnnotationsTransformerBuildItem registerNamedPersistenceUnitAdapters(OrmTenantBuildTimeConfig config,
            HibernateOrmConfig hibernateOrmConfig) {
        Set<String> persistenceUnitNames = configuredNamedPersistenceUnits(config);
        validateNamedPersistenceUnits(persistenceUnitNames, hibernateOrmConfig.namedPersistenceUnits());

        return new AnnotationsTransformerBuildItem(AnnotationTransformation.forClasses()
                .whenClass(ORM_TENANT_RESOLVER_ADAPTER)
                .transform(context -> persistenceUnitNames.forEach(name -> context.add(
                        AnnotationInstance.builder(PERSISTENCE_UNIT_EXTENSION)
                                .add("value", name)
                                .build()))));
    }

    private static void validateNamedPersistenceUnits(Set<String> selectedNames,
            Map<String, HibernateOrmConfigPersistenceUnit> persistenceUnits) {
        if (selectedNames.isEmpty()) {
            return;
        }

        List<String> errors = new ArrayList<>();
        for (String selectedName : selectedNames) {
            HibernateOrmConfigPersistenceUnit persistenceUnit = persistenceUnits.get(selectedName);
            if (persistenceUnit == null) {
                List<String> availableNames = persistenceUnits.keySet().stream().sorted().toList();
                errors.add("Unknown named Hibernate ORM persistence unit '" + selectedName
                        + "'. Available named persistence units: " + availableNames + ".");
                continue;
            }

            if (persistenceUnit.multitenant().isEmpty()) {
                errors.add("Hibernate ORM persistence unit '" + selectedName
                        + "' is not configured for multitenancy. Configure "
                        + "quarkus.hibernate-orm.\"" + selectedName + "\".multitenant before selecting it.");
            }
        }

        if (!errors.isEmpty()) {
            errors.sort(Comparator.naturalOrder());
            throw new ConfigurationException(NAMED_PERSISTENCE_UNITS_PROPERTY + " is invalid: "
                    + String.join(" ", errors));
        }
    }

    private static Set<String> configuredNamedPersistenceUnits(OrmTenantBuildTimeConfig config) {
        Set<String> configuredNames = new TreeSet<>();
        for (String configuredName : config.namedPersistenceUnits().orElse(Set.of())) {
            String name = configuredName.trim();
            if (name.isEmpty()) {
                throw new ConfigurationException(NAMED_PERSISTENCE_UNITS_PROPERTY
                        + " must not contain a blank persistence-unit name.");
            }
            if (PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME.equals(name)) {
                throw new ConfigurationException(NAMED_PERSISTENCE_UNITS_PROPERTY
                        + " accepts named persistence units only; the default persistence unit is always integrated.");
            }
            configuredNames.add(name);
        }
        return configuredNames;
    }
}
