package io.quarkiverse.multitenancy.http.deployment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class MultiTenantHttpProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest UNIT_TEST = new QuarkusUnitTest()
            .withApplicationRoot(jar -> jar
                    .addClasses(MultiTenantHttpProcessor.class));

    @Test
    void extensionShouldBoot() {
        // if we reach here, the build step executed successfully
        assertTrue(true);
    }
}
