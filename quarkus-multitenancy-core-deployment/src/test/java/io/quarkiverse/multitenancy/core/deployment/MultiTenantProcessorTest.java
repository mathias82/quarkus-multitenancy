package io.quarkiverse.multitenancy.core.deployment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class MultiTenantProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar
                    .addClasses(MultiTenantProcessor.class));

    @Test
    void extensionShouldBoot() {
        // if we reach here, the build step executed successfully
        assertTrue(true);
    }
}
