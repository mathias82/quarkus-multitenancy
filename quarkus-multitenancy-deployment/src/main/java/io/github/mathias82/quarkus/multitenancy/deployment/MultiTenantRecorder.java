package io.github.mathias82.quarkus.multitenancy.deployment;

import io.quarkus.runtime.annotations.Recorder;

/**
 * Recorder class for runtime initialization.
 * For now, it's empty, but later you can add initialization logic
 * that must run at runtime-init.
 */
@Recorder
public class MultiTenantRecorder {

    public void init() {
        // Placeholder for future runtime initialization logic.
    }
}
