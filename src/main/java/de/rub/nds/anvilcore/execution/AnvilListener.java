/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.execution;

import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.reporting.AnvilReport;
import org.junit.platform.launcher.TestPlan;

public interface AnvilListener {

    default void gotConfig(AnvilTestConfig anvilConfig, String additionalConfig) {}

    default void onAborted() {}

    default boolean beforeStart(TestPlan testPlan, long totalTests) {
        return true;
    }

    default void onStarted() {}

    default void onReportFinished(AnvilReport anvilReport) {}

    default void onTestRunFinished(AnvilTestRun testRun) {}

    default void onTestCaseFinished(AnvilTestCase testCase, String testId) {}
}
