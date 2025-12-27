package io.github.mathias82.quarkus.multitenancy.http.deployment;

import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiTenantHttpProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar
                    .addClasses(MultiTenantHttpProcessor.class)
            );

    @Test
    void extensionShouldBoot() {
        // if we reach here, the build step executed successfully
        assertTrue(true);
    }
}
