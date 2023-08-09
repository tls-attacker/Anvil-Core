package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.execution.AnvilListener;
import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rub.nds.anvilcore.teststate.reporting.AnvilJsonMapper;
import de.rub.nds.anvilcore.teststate.reporting.ScoreContainer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static AnvilContext instance;

    private final AnvilTestConfig config;
    private final String configString;
    private final AnvilJsonMapper mapper;
    private AnvilListener listener;

    private int testStrength = 2;
    private final ParameterIdentifierProvider parameterIdentifierProvider;

    private long totalTests = 0;
    private long testsDone = 0;
    private final Date creationTime = new Date();
    private Date testStartTime;
    private final ScoreContainer scoreContainer = null;

    private final Map<String, AnvilTestRun> activeTestRuns = new HashMap<>();
    private final Map<TestResult, List<String>> resultTestMap = new HashMap<>();
    private final Map<String, Boolean> finishedTests = new HashMap<>();
    private boolean aborted = false;

    public static synchronized AnvilContext getInstance() {
        return instance;
    }

    public static AnvilContext createInstance(
            AnvilTestConfig config, String configString, ParameterIdentifierProvider provider) {
        AnvilContext newContext = new AnvilContext(config, configString, provider);
        instance = newContext;
        return newContext;
    }

    private AnvilContext(
            AnvilTestConfig config, String configString, ParameterIdentifierProvider provider) {
        this.parameterIdentifierProvider = provider;
        this.config = config;
        this.configString = configString;
        this.mapper = new AnvilJsonMapper(config);
    }

    public void abortRemainingTests() {
        aborted = true;
    }

    public boolean isAborted() {
        return aborted;
    }

    public ParameterIdentifierProvider getParameterIdentifierProvider() {
        return parameterIdentifierProvider;
    }

    public synchronized Map<String, AnvilTestRun> getActiveTestRuns() {
        return activeTestRuns;
    }

    public synchronized AnvilTestRun getTestResult(String uniqueId) {
        return activeTestRuns.get(uniqueId);
    }

    public synchronized void addActiveTestRun(AnvilTestRun testResult) {
        activeTestRuns.put(testResult.getUniqueId(), testResult);
    }

    public synchronized void testFinished(String uniqueId) {
        finishedTests.put(uniqueId, true);
        // TODO scoreContainer.merge(activeTestRuns.get(uniqueId).getScoreContainer());
        AnvilTestRun finishedContainer = activeTestRuns.remove(uniqueId);
        testsDone++;

        long timediff = new Date().getTime() - creationTime.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timediff);
        long remainingSecondsInMillis = timediff - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingSecondsInMillis);
        LOGGER.info(
                String.format(
                        "%d/%d Tests finished (in %02d:%02d)",
                        testsDone, totalTests, minutes, seconds));

        if (listener != null) {
            listener.onTestRunFinished(finishedContainer);
        }
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

    public synchronized Date getCreationTime() {
        return creationTime;
    }

    public Date getTestStartTime() {
        return testStartTime;
    }

    public void setTestStartTime(Date testStartTime) {
        this.testStartTime = testStartTime;
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

    public ScoreContainer getScoreContainer() {
        return scoreContainer;
    }

    public Map<TestResult, List<String>> getResultTestMap() {
        return resultTestMap;
    }

    public void addTestResult(TestResult result, String testName) {
        getResultTestMap().computeIfAbsent(result, k -> new LinkedList<>());
        getResultTestMap().get(result).add(testName);
    }

    public AnvilListener getListener() {
        return listener;
    }

    public void setListener(AnvilListener listener) {
        this.listener = listener;
    }

    public AnvilTestConfig getConfig() {
        return config;
    }

    public AnvilJsonMapper getMapper() {
        return mapper;
    }

    public String getConfigString() {
        return configString;
    }
}
