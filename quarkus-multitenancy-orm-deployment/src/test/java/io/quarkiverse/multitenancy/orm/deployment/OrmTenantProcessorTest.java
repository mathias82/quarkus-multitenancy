package io.quarkiverse.multitenancy.orm.deployment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class OrmTenantProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar.addClasses(OrmTenantProcessor.class));

    @Test
    void extensionShouldBoot() {
        assertTrue(true);
    }
}
