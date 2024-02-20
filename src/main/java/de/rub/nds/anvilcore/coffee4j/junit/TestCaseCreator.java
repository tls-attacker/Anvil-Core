/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.coffee4j.junit;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rwth.swc.coffee4j.model.Combination;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestCaseCreator implements BeforeTestExecutionCallback {

    private Combination testInput;

    TestCaseCreator(Combination testInput) {
        this.testInput = testInput;
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        AnvilTestCase testCase =
                new AnvilTestCase(
                        ParameterCombination.fromCombination(
                                testInput, DerivationScope.fromExtensionContext(extensionContext)),
                        extensionContext);
        extensionContext
                .getStore(ExtensionContext.Namespace.GLOBAL)
                .put(AnvilTestCase.class.getName(), testCase);
    }
}
