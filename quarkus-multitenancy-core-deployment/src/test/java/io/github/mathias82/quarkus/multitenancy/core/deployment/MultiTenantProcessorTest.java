package io.github.mathias82.quarkus.multitenancy.core.deployment;

import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;

class MultiTenantProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar
                    .addClasses(MultiTenantProcessor.class)
            );

    @Test
    void extensionShouldBoot() {
        // if we reach here, the build step executed successfully
        assertTrue(true);
    }
}

