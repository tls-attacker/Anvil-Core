/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.coffee4j.model;

import de.rub.nds.anvilcore.model.IpmProvider;
import de.rwth.swc.coffee4j.junit.provider.model.ModelSource;
import java.lang.annotation.*;

/** This is an extended copy of the ModelFromMethod of Coffee4j. */
@Inherited
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ModelSource(IpmProvider.class)
public @interface ModelFromScope {
    String modelType() default "ALL_PARAMETERS";
}
