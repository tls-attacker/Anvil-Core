/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.anvilcore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides the interface to define methods for
 * DerivationTypes to set specific Coffee4j constraints
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExplicitModelingConstraints {
    Class<?> clazz() default Object.class;
    String[] affectedTypes();
    String[] affectedScopes();
    String[] methods();
}