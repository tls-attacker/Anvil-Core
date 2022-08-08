package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.model.DefaultModelType;
import de.rub.nds.anvilcore.model.ModelType;
import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AnvilContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static AnvilContext instance;

    private ApplicationSpecificContextDelegate applicationSpecificContextDelegate;
    private int testStrength = 2;
    private final List<ModelType> knownModelTypes;

    private long totalTests = 0;
    private long testsDone = 0;
    private long testsDisabled = 0;
    private long testsFailed = 0;
    private long testsSucceeded = 0;

    private final Map<String, AnvilTestStateContainer> testResults = new HashMap<>();
    private final Map<String, Boolean> finishedTests = new HashMap<>();

    synchronized public static AnvilContext getInstance() {
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

    public void setApplicationSpecificContextDelegate(ApplicationSpecificContextDelegate applicationSpecificContextDelegate) {
        this.applicationSpecificContextDelegate = applicationSpecificContextDelegate;
    }

    public List<ModelType> getKnownModelTypes() {
        return knownModelTypes;
    }

    public synchronized Map<String, AnvilTestStateContainer> getTestResults() {
        return testResults;
    }

    synchronized public AnvilTestStateContainer getTestResult(String uniqueId) {
        return testResults.get(uniqueId);
    }

    synchronized public void addTestResult(AnvilTestStateContainer testResult) {
        testResults.put(testResult.getUniqueId(), testResult);
    }

    synchronized public void testFinished(String uniqueId) {
        finishedTests.put(uniqueId, true);
        // TODO score container
        testResults.remove(uniqueId);
        testsDone++;

        applicationSpecificContextDelegate.onTestFinished(uniqueId);
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

    synchronized public void testDisabled() {
        testsDisabled++;
    }

}
