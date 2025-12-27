package io.github.mathias82.quarkus.multitenancy.orm.deployment;

import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrmTenantProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar.addClasses(OrmTenantProcessor.class));

    @Test
    void extensionShouldBoot() {
        assertTrue(true);
    }
}
