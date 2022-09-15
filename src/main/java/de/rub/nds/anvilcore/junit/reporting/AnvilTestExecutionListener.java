package de.rub.nds.anvilcore.junit.reporting;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;
import de.rub.nds.anvilcore.teststate.reporting.TestSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AnvilTestExecutionListener implements TestExecutionListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private long startTime;
    private final Map<String, Long> elapsedTimes = new HashMap<>();

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        startTime = System.currentTimeMillis();
        LOGGER.trace("Started execution of " + testPlan.toString());
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        LOGGER.trace("Execution of " + testPlan.toString() + " finished");
    }

    /**
     * Generate and return a test summary describing the tests and their status.
     *
     * @return a test summary
     */
    public TestSummary getTestSummary() {
        // TODO: Factor out singletons.
        //
        // Singletons are an anti-pattern and introduce a global state that
        // blurs interdependencies between different code fragments and also
        // makes testing harder. Also, there no reason why this uses singletons
        // at all, because we could easily pass the approriate `AnvilContext`
        // object to the constructor or this method.
        final TestSummary testSummary = AnvilFactoryRegistry.get().getTestSummaryFactory().getInstance();
        testSummary.setElapsedTime(System.currentTimeMillis() - startTime);
        testSummary.setIdentifier("todo");      // TODO
        testSummary.setDate(AnvilContext.getInstance().getStartTime());
        testSummary.setTestsDisabled(AnvilContext.getInstance().getTestsDisabled());
        testSummary.setTestsFailed(AnvilContext.getInstance().getTestsFailed());
        testSummary.setTestsSucceeded(AnvilContext.getInstance().getTestsSucceeded());
        testSummary.setScoreContainer(AnvilContext.getInstance().getScoreContainer());

        return testSummary;
    }

    /**
     * Write {@link TestSummary} to {@code stream}.
     *
     * <p>This should be called when execution of test plan has already finished.
     *
     * @param stream output stream to write summary to
     * @throws IOException on stream write failure
     */
    public void writeTestSummary(final OutputStream stream) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final TestSummary testSummary = getTestSummary();
        objectMapper.writeValue(stream, testSummary);
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
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        LOGGER.trace(testIdentifier.getDisplayName() + " finished");
        if (testIdentifier.isContainer()) {
            Long startTime = elapsedTimes.get(testIdentifier.getUniqueId());
            if (startTime != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                elapsedTimes.put(testIdentifier.getUniqueId(), elapsedTime);
            }
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {

    }
}
