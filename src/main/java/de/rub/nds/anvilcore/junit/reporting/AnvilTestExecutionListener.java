package de.rub.nds.anvilcore.junit.reporting;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rub.nds.anvilcore.teststate.reporting.AnvilReport;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final AnvilTestConfig testConfig;

    public AnvilTestExecutionListener(AnvilTestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        startTime = System.currentTimeMillis();
        LOGGER.trace("Started execution of " + testPlan.toString());
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        LOGGER.trace("Execution of " + testPlan.toString() + " finished");
        try {
            writeTestSummary();
        } catch (IOException e) {
            LOGGER.error("Failed to write post execution report", e);
        }
    }

    /**
     * Generate and return a test summary describing the tests and their status.
     *
     * @return a test summary
     */
    public AnvilReport getReport() {
        final AnvilReport report = new AnvilReport();
        report.setElapsedTime(System.currentTimeMillis() - startTime);
        report.setIdentifier(testConfig.getIdentifier());
        report.setDate(AnvilContext.getInstance().getStartTime());
        report.setTestsDisabled(
                AnvilContext.getInstance()
                        .getResultTestMap()
                        .computeIfAbsent(TestResult.DISABLED, k -> new LinkedList<>())
                        .size());
        report.setTestsFullyFailed(
                AnvilContext.getInstance()
                        .getResultTestMap()
                        .computeIfAbsent(TestResult.FULLY_FAILED, k -> new LinkedList<>())
                        .size());
        report.setTestsStrictlySucceeded(
                AnvilContext.getInstance()
                        .getResultTestMap()
                        .computeIfAbsent(TestResult.STRICTLY_SUCCEEDED, k -> new LinkedList<>())
                        .size());
        report.setTestsPartiallyFailed(
                AnvilContext.getInstance()
                        .getResultTestMap()
                        .computeIfAbsent(TestResult.PARTIALLY_FAILED, k -> new LinkedList<>())
                        .size());
        report.setTestsConceptuallySucceeded(
                AnvilContext.getInstance()
                        .getResultTestMap()
                        .computeIfAbsent(TestResult.CONCEPTUALLY_SUCCEEDED, k -> new LinkedList<>())
                        .size());
        report.setScoreContainer(AnvilContext.getInstance().getScoreContainer());

        return report;
    }

    /**
     * Write {@link AnvilReport} to {@code stream}.
     *
     * <p>This should be called when execution of test plan has already finished.
     *
     * @throws IOException on stream write failure
     */
    public void writeTestSummary() throws IOException {
        FileOutputStream stream =
                new FileOutputStream(testConfig.getOutputFolder() + "/report.json");
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(
                objectMapper
                        .getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final AnvilReport report = getReport();
        objectMapper.writeValue(stream, report);
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
