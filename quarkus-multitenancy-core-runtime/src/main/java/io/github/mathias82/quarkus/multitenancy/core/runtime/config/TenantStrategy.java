package io.github.mathias82.quarkus.multitenancy.core.runtime.config;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({TYPE, FIELD, PARAMETER})
public @interface TenantStrategy {
    @Nonbinding String value() default "";
}
