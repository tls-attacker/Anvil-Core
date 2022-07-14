package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.model.DefaultModelType;
import de.rub.nds.anvilcore.model.ModelBasedIpmFactory;
import de.rub.nds.anvilcore.model.ModelType;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterType;
import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;
import de.rub.nds.anvilcore.teststate.DefaultScoreContainerFactory;
import de.rub.nds.anvilcore.teststate.ScoreContainerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AnvilContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static AnvilContext instance;

    private ApplicationSpecificContextDelegate applicationSpecificContextDelegate;
    private int testStrength = 2;

    private final Map<ParameterType, ParameterFactory> knownParameters = new HashMap<>();
    private final List<ModelType> knownModelTypes;
    private ModelBasedIpmFactory modelBasedIpmFactory;
    private ScoreContainerFactory scoreContainerFactory = new DefaultScoreContainerFactory();

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

    public Map<ParameterType, ParameterFactory> getKnownParameters() {
        return knownParameters;
    }

    public void addParameterTypes(ParameterType[] parameterTypes, ParameterFactory associatedFactory) {
        for (ParameterType parameterType : parameterTypes) {
            if (knownParameters.containsKey(parameterType)) {
                LOGGER.warn("Parameter type " + parameterType.toString() + " already exists");
            }
            knownParameters.put(parameterType, associatedFactory);
        }
    }

    public ParameterFactory getParameterFactory(ParameterType parameterType) {
        if (!knownParameters.containsKey(parameterType)) {
            throw new IllegalArgumentException("Parameter " + parameterType + " is not known");
        }
        return knownParameters.get(parameterType);
    }

    public List<ModelType> getKnownModelTypes() {
        return knownModelTypes;
    }

    public ModelBasedIpmFactory getModelBasedIpmFactory() {
        return modelBasedIpmFactory;
    }

    public void setModelBasedIpmFactory(ModelBasedIpmFactory modelBasedIpmFactory) {
        this.modelBasedIpmFactory = modelBasedIpmFactory;
    }

    public ScoreContainerFactory getScoreContainerFactory() {
        return scoreContainerFactory;
    }

    public synchronized void setScoreContainerFactory(ScoreContainerFactory scoreContainerFactory) {
        this.scoreContainerFactory = scoreContainerFactory;
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
