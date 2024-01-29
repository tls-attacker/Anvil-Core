/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.coffee4j.junit;

import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rwth.swc.coffee4j.junit.CombinatorialTestMethodContext;
import de.rwth.swc.coffee4j.model.Combination;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestCaseResolver implements ParameterResolver {

    private final CombinatorialTestMethodContext methodContext;
    private final Combination testInput;

    TestCaseResolver(CombinatorialTestMethodContext methodContext, Combination testInput) {
        this.methodContext = methodContext;
        this.testInput = testInput;
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(AnvilTestCase.class);
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return extensionContext
                .getStore(ExtensionContext.Namespace.GLOBAL)
                .get(AnvilTestCase.class.getName());
    }
}
