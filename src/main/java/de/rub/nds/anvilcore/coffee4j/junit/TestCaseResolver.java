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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestCaseResolver implements ParameterResolver {

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
        AnvilTestCase testCase =
                (AnvilTestCase)
                        extensionContext
                                .getStore(ExtensionContext.Namespace.GLOBAL)
                                .get(AnvilTestCase.class.getName());
        if (testCase != null) {
            return testCase;
        } else {
            // for non-combinatorial tests, that want to use a testcase, we generate an empty one
            AnvilTestCase emptyCase = new AnvilTestCase(null, extensionContext);
            extensionContext
                    .getStore(ExtensionContext.Namespace.GLOBAL)
                    .put(AnvilTestCase.class.getName(), emptyCase);
            return emptyCase;
        }
    }
}
