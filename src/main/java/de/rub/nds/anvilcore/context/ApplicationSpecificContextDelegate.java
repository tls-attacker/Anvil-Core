/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.teststate.AnvilTestRun;

public interface ApplicationSpecificContextDelegate {
    default void onTestFinished(String uniqueId, AnvilTestRun finishedContainer) {}
}
