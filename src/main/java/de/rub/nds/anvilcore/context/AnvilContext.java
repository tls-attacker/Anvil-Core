package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.model.DefaultModelType;
import de.rub.nds.anvilcore.model.ModelType;
import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rub.nds.anvilcore.teststate.reporting.ScoreContainer;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static AnvilContext instance;

    private ApplicationSpecificContextDelegate applicationSpecificContextDelegate;
    private int testStrength = 2;
    private final List<ModelType> knownModelTypes;

    private TestEndpointType evaluatedEndpoint = TestEndpointType.BOTH;
    private long totalTests = 0;
    private long testsDone = 0;
    private long testsDisabled = 0;
    private long testsFailed = 0;
    private long testsSucceeded = 0;
    private final Date startTime = new Date();
    private final ScoreContainer scoreContainer =
            AnvilFactoryRegistry.get().getScoreContainerFactory().getInstance();

    private final Map<String, AnvilTestStateContainer> testResults = new HashMap<>();
    private final Map<String, TestResult> aggregatedTestResult = new HashMap<>();
    private final Map<String, Boolean> finishedTests = new HashMap<>();

    public static synchronized AnvilContext getInstance() {
        if (AnvilContext.instance == null) {
            AnvilContext.instance = new AnvilContext();
        }
        return AnvilContext.instance;
    }

    private AnvilContext() {
        knownModelTypes = new ArrayList<>();
        knownModelTypes.addAll(Arrays.asList(DefaultModelType.values()));
    }

    public static void setInstance(AnvilContext instance) {
        AnvilContext.instance = instance;
    }

    public ApplicationSpecificContextDelegate getApplicationSpecificContextDelegate() {
        return applicationSpecificContextDelegate;
    }

    public void setApplicationSpecificContextDelegate(
            ApplicationSpecificContextDelegate applicationSpecificContextDelegate) {
        this.applicationSpecificContextDelegate = applicationSpecificContextDelegate;
    }

    public List<ModelType> getKnownModelTypes() {
        return knownModelTypes;
    }

    public synchronized Map<String, AnvilTestStateContainer> getTestResults() {
        return testResults;
    }

    public synchronized AnvilTestStateContainer getTestResult(String uniqueId) {
        return testResults.get(uniqueId);
    }

    public synchronized void addTestStateContainer(AnvilTestStateContainer testResult) {
        testResults.put(testResult.getUniqueId(), testResult);
    }

    public synchronized void testFinished(String uniqueId) {
        finishedTests.put(uniqueId, true);
        scoreContainer.merge(testResults.get(uniqueId).getScoreContainer());
        AnvilTestStateContainer finishedContainer = testResults.remove(uniqueId);
        testsDone++;

        applicationSpecificContextDelegate.onTestFinished(uniqueId, finishedContainer);
    }

    public synchronized Map<String, Boolean> getFinishedTests() {
        return finishedTests;
    }

    public synchronized int getTestStrength() {
        return testStrength;
    }

    public synchronized void setTestStrength(int testStrength) {
        this.testStrength = testStrength;
    }

    public synchronized boolean testIsFinished(String uniqueId) {
        return finishedTests.containsKey(uniqueId);
    }

    public synchronized void testSucceeded() {
        testsSucceeded++;
    }

    public synchronized void testFailed() {
        testsFailed++;
    }

    public synchronized void testDisabled() {
        testsDisabled++;
    }

    public synchronized Date getStartTime() {
        return startTime;
    }

    public long getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(long totalTests) {
        this.totalTests = totalTests;
    }

    public long getTestsDone() {
        return testsDone;
    }

    public long getTestsDisabled() {
        return testsDisabled;
    }

    public long getTestsFailed() {
        return testsFailed;
    }

    public long getTestsSucceeded() {
        return testsSucceeded;
    }

    public ScoreContainer getScoreContainer() {
        return scoreContainer;
    }

    public TestEndpointType getEvaluatedEndpoint() {
        return evaluatedEndpoint;
    }

    public void setEvaluatedEndpoint(TestEndpointType evaluatedEndpoint) {
        this.evaluatedEndpoint = evaluatedEndpoint;
    }

    public Map<String, TestResult> getAggregatedTestResult() {
        return aggregatedTestResult;
    }
}
