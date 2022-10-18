/**
 * Anvil-Core - A framework for modeling tests
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rub.nds.anvilcore.annotation;

import de.rub.nds.anvilcore.model.parameter.ParameterType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Excludes a parameter from the IPM.
 *
 * Repeatable alternative to {@link IpmLimitations}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(ExcludeParameters.class)
public @interface ExcludeParameter {
    String value();
};
