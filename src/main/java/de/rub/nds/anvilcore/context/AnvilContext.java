/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.rub.nds.anvilcore.execution.AnvilListener;
import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rub.nds.anvilcore.teststate.reporting.AnvilJsonMapper;
import de.rub.nds.anvilcore.teststate.reporting.MetadataFetcher;
import de.rub.nds.anvilcore.teststate.reporting.ScoreContainer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static AnvilContext instance;

    private final AnvilTestConfig config;
    private final String configString;
    private final AnvilJsonMapper mapper;
    private final MetadataFetcher metadataFetcher;
    private AnvilListener listener;

    private final ParameterIdentifierProvider parameterIdentifierProvider;

    private long totalTests = 0;
    private long testsDone = 0;
    private long testCases = 0;
    private final Date creationTime = new Date();
    private Date testStartTime;

    @JsonProperty("Score")
    private final ScoreContainer overallScoreContainer = new ScoreContainer();

    /**
     * A Map that keeps track of all the active test runs in the current context. The keys in this
     * map are String objects representing unique IDs of test runs, and the values are AnvilTestRun
     * objects representing the test runs themselves.
     */
    private final Map<String, AnvilTestRun> activeTestRuns = new HashMap<>();

    /**
     * A Map that holds the test results. The keys are TestResult objects, and the values are
     * List<String> objects representing the unique IDs of the tests.
     */
    private final Map<TestResult, List<String>> resultsTestRuns = new HashMap<>();

    /**
     * A Map that holds the finished tests. The keys are String objects representing unique IDs of
     * tests, and the values are Boolean objects indicating whether the test is finished.
     */
    private final Map<String, Boolean> finishedTestRuns = new HashMap<>();

    /**
     * A Map that holds the failure details of all failed test cases by test run. The keys are the
     * test run unique IDs and the values are the test case failure details per test run.
     */
    private final Map<String, List<String>> detailsFailedTestCases = new HashMap<>();

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
        this.metadataFetcher = new MetadataFetcher();
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

    public synchronized AnvilTestRun getTestRun(String uniqueId) {
        return activeTestRuns.get(uniqueId);
    }

    public synchronized void addActiveTestRun(AnvilTestRun testRun) {
        activeTestRuns.put(testRun.getUniqueId(), testRun);
    }

    private synchronized void addDetailsFailedTestCases(AnvilTestRun testRun) {
        List<AnvilTestCase> failedTestCases =
                testRun.getTestCases().stream()
                        .filter(
                                testCase ->
                                        testCase.getTestResult() == TestResult.FULLY_FAILED
                                                || testCase.getTestResult()
                                                        == TestResult.PARTIALLY_FAILED)
                        .collect(Collectors.toList());

        failedTestCases.stream()
                .map(AnvilTestCase::getFailureDetails)
                .forEach(
                        failureDetail ->
                                detailsFailedTestCases
                                        .computeIfAbsent(
                                                testRun.getUniqueId(), k -> new LinkedList<>())
                                        .add(failureDetail));
    }

    public synchronized Map<String, List<String>> getDetailsFailedTestCases() {
        return detailsFailedTestCases;
    }

    public synchronized void testFinished(AnvilTestRun testRun) {
        String testRunUniqueId = testRun.getUniqueId();
        addDetailsFailedTestCases(testRun);
        finishedTestRuns.put(testRunUniqueId, true);
        overallScoreContainer.merge(activeTestRuns.get(testRunUniqueId).getScoreContainer());
        AnvilTestRun finishedContainer = activeTestRuns.remove(testRunUniqueId);
        testsDone++;
        if (finishedContainer.getTestCases() != null) {
            testCases += finishedContainer.getTestCases().size();
        }

        long timediff = new Date().getTime() - creationTime.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timediff);
        long remainingSecondsInMillis = timediff - TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingSecondsInMillis);
        String uniqueIdCompact =
                testRunUniqueId.replaceAll(
                        "\\[engine:junit-jupiter]/|\\(org\\.junit\\.jupiter\\.params\\.aggregator\\.ArgumentsAccessor, de\\.rub\\.nds\\.tlstest\\.framework\\.execution\\.WorkflowRunner\\)",
                        "");

        LOGGER.info(
                String.format(
                        "%d/%d Tests finished (in %02d:%02d). Method: %s",
                        testsDone, totalTests, minutes, seconds, uniqueIdCompact));

        if (listener != null) {
            listener.onTestRunFinished(finishedContainer);
        }
    }

    public synchronized Map<String, Boolean> getFinishedTestRuns() {
        return finishedTestRuns;
    }

    public synchronized boolean testIsFinished(String uniqueId) {
        return finishedTestRuns.containsKey(uniqueId);
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

    public long getTestCases() {
        return testCases;
    }

    public long getTestsDone() {
        return testsDone;
    }

    public ScoreContainer getOverallScoreContainer() {
        return overallScoreContainer;
    }

    public Map<TestResult, List<String>> getResultsTestRuns() {
        return resultsTestRuns;
    }

    public synchronized void addTestResult(TestResult result, AnvilTestRun testRun) {
        getResultsTestRuns().computeIfAbsent(result, k -> new LinkedList<>());
        getResultsTestRuns().get(result).add(testRun.getUniqueId());
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

    public MetadataFetcher getMetadataFetcher() {
        return metadataFetcher;
    }

    public String getConfigString() {
        return configString;
    }
}
