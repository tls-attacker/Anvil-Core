/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit.simpletest;

import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** */
public class SimpleTestExecutionCallback implements AfterTestExecutionCallback {

    public SimpleTestExecutionCallback() {}

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        SimpleTestManager testManager =
                SimpleTestManagerContainer.getInstance().getManagerByExtension(extensionContext);
        testManager.testCompleted();
        if (testManager.allTestsFinished()) {
            AnvilTestRun.forExtensionContext(extensionContext).finish();
        }
    }
}
