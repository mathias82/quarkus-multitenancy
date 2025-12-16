package io.github.mathias82.quarkus.multitenancy.core.runtime.config;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target(TYPE)
public @interface TenantStrategy {
    String value();
}