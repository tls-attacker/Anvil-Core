package de.rub.nds.anvilcore.execution;

import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.reporting.AnvilReport;
import org.junit.platform.launcher.TestPlan;

public interface AnvilListener {

    default void gotConfig(AnvilTestConfig anvilConfig, String additionalConfig) {}

    default void onAborted() {}
    ;

    default boolean beforeStart(TestPlan testPlan) {
        return true;
    }

    default void onStarted() {}

    default void onReportFinished(AnvilReport anvilReport) {}

    default void onTestRunFinished(AnvilTestRun testRun) {}

    default void onTestCaseFinished(AnvilTestCase testCase, String className, String methodName) {}
}
