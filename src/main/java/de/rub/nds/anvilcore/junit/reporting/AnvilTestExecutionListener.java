package de.rub.nds.anvilcore.junit.reporting;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.teststate.reporting.AnvilReport;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class AnvilTestExecutionListener implements TestExecutionListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private long startTime;
    private final Map<String, Long> elapsedTimes = new HashMap<>();

    public AnvilTestExecutionListener() {}

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        AnvilContext.getInstance().setTestStartTime(new Date());
        LOGGER.trace("Started execution of " + testPlan.toString());
        if (AnvilContext.getInstance().getListener() != null) {
            AnvilContext.getInstance().getListener().onStarted();
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        LOGGER.trace("Execution of " + testPlan.toString() + " finished");
        AnvilReport anvilReport = new AnvilReport(AnvilContext.getInstance(), false);
        AnvilContext.getInstance().getMapper().saveReportToPath(anvilReport);
        if (AnvilContext.getInstance().getListener() != null) {
            AnvilContext.getInstance().getListener().onReportFinished(anvilReport);
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        LOGGER.trace(testIdentifier.getDisplayName() + " skipped, due to " + reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        LOGGER.trace(testIdentifier.getDisplayName() + " started");
        if (testIdentifier.isContainer()) {
            elapsedTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Override
    public void executionFinished(
            TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        logTestFinished(testExecutionResult, testIdentifier);
        if (testIdentifier.isContainer()) {
            Long startTime = elapsedTimes.get(testIdentifier.getUniqueId());
            if (startTime != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                elapsedTimes.put(testIdentifier.getUniqueId(), elapsedTime);
            }
        }
    }

    public void logTestFinished(
            TestExecutionResult testExecutionResult, TestIdentifier testIdentifier) {
        if (testExecutionResult.getThrowable().isPresent() && testIdentifier.isContainer()) {
            LOGGER.error(
                    "Internal exception during execution of test container created for test {}. Exception: ",
                    testIdentifier.getDisplayName(),
                    testExecutionResult.getThrowable().get());
        } else {
            LOGGER.trace(testIdentifier.getDisplayName() + " finished");
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {}
}
