/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.rub.nds.anvilcore.annotation.AnvilTest;
import de.rub.nds.anvilcore.annotation.NonCombinatorialAnvilTest;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.Utils;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.reporting.MetadataFetcher;
import de.rub.nds.anvilcore.teststate.reporting.ScoreContainer;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AnvilTestRun {
    private static final Logger LOGGER = LogManager.getLogger();

    // set when the last finished has been reported by junit
    @JsonIgnore private boolean finished = false;
    // set when coffee4j reports that the input group has been finished
    @JsonIgnore private boolean readyForCompletion = false;

    private final long startTime = System.currentTimeMillis();
    private int resultRaw = 0;
    private String uniqueId;

    @JsonProperty("TestId")
    private String testId;

    private Method testMethod;
    private Class<?> testClass;

    @JsonProperty("HasStateWithAdditionalResultInformation")
    private boolean hasStateWithAdditionalResultInformation() {
        if (testCases == null) {
            return false;
        }
        return testCases.stream()
                .anyMatch(
                        tC ->
                                tC.getAdditionalResultInformation() != null
                                        && !tC.getAdditionalResultInformation().isEmpty());
    }

    @JsonProperty("HasVaryingAdditionalResultInformation")
    private boolean hasVaryingAdditionalResultInformation() {
        if (testCases == null) {
            return false;
        }
        Optional<AnvilTestCase> testCase =
                testCases.stream()
                        .filter(
                                tC ->
                                        tC.getAdditionalResultInformation() != null
                                                && !tC.getAdditionalResultInformation().isEmpty())
                        .findFirst();
        if (testCase.isEmpty()) return false;
        List<String> additionalInformation = testCase.get().getAdditionalResultInformation();
        return !testCases.stream()
                .filter(
                        tC ->
                                tC.getAdditionalResultInformation() != null
                                        && !tC.getAdditionalResultInformation().isEmpty())
                .allMatch(
                        tC ->
                                new HashSet<>(tC.getAdditionalResultInformation())
                                        .containsAll(additionalInformation));
    }

    @JsonProperty("TestCases")
    private List<AnvilTestCase> testCases = new ArrayList<>();

    @JsonProperty("Result")
    private TestResult result;

    @JsonProperty("DisabledReason")
    private String disabledReason;

    @JsonProperty("FailedReason")
    private String failedReason;

    @JsonProperty("ElapsedTime")
    private long elapsedTime = 0; // todo set elapsed time

    @JsonProperty("FailureInducingCombinations")
    private List<ParameterCombination> failureInducingCombinations;

    @JsonProperty("Score")
    private ScoreContainer scoreContainer;

    @JsonProperty("TestMethod")
    private String getTestMethodString() {
        return testMethod.getName();
    }

    @JsonProperty("TestClass")
    private String getTestClassString() {
        return testClass.getName();
    }

    @JsonProperty("CaseCount")
    private int getCaseCount() {
        return testCases.size();
    }

    @JsonUnwrapped
    private Map<?, ?> getMetadata() {
        return AnvilContext.getInstance().getMetadataFetcher().getRawMetadata(testId);
    }

    private Map<TestResult, List<AnvilTestCase>> groupTestCasesByResult() {
        Map<TestResult, List<AnvilTestCase>> testCasesByResult = new EnumMap<>(TestResult.class);
        testCases.forEach(
                testCase -> {
                    TestResult testCaseResult = testCase.getTestResult();
                    testCasesByResult
                            .computeIfAbsent(testCaseResult, mapKey -> new ArrayList<>())
                            .add(testCase);
                });

        return testCasesByResult;
    }

    private List<TestResult> getFailureTestResults() {
        return Arrays.asList(TestResult.FULLY_FAILED, TestResult.PARTIALLY_FAILED);
    }

    private List<AnvilTestCase> filterTestCasesByResult(TestResult testResult) {
        return testCases.stream()
                .filter(testCase -> testCase.getTestResult() == testResult)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> filterUniqueFailedTestCases(TestResult testResult) {
        if (!getFailureTestResults().contains(testResult)) {
            String failureResults =
                    getFailureTestResults().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(" or "));
            throw new IllegalArgumentException("Test result must be either " + failureResults);
        }

        Map<String, Integer> failedTestCasesDetails = new HashMap<>();
        List<AnvilTestCase> failedTestCases = filterTestCasesByResult(testResult);

        // create a frequency map of failure details for all failed test cases
        for (AnvilTestCase failedTestCase : failedTestCases) {
            String failureDetail = getTestCaseFailureDetails(failedTestCase);
            failedTestCasesDetails.merge(failureDetail, 1, Integer::sum);
        }

        return failedTestCasesDetails;
    }

    /**
     * Generates a string containing the failure reason and additional result information of a
     * failed test case.
     *
     * @param failedTestCase The failed test case
     * @return The generated string
     */
    private String getTestCaseFailureDetails(AnvilTestCase failedTestCase) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(
                String.format(
                        "Failure reason: %s.",
                        failedTestCase.getFailedReason() != null
                                ? failedTestCase.getFailedReason()
                                : "undefined"));
        if (failedTestCase.getAdditionalResultInformation() != null) {
            logMessage.append(
                    String.format(
                            "Additional result info: %s.",
                            failedTestCase.getAdditionalResultInformation()));
        }

        return logMessage.toString();
    }

    /**
     * This method generates a summary of the failure details for a given test result.
     *
     * @param testResult The test result for which the failure details summary is to be generated.
     * @param failedTestCaseDetails A map where the keys are the detail messages of the failed test
     *     cases and the values are the counts of how many times each detail message appears.
     * @return A string containing the failure details summary for the given test result.
     */
    private String buildTestCaseFailureDetailsSummary(
            TestResult testResult, Map<String, Integer> failedTestCaseDetails) {
        StringJoiner logMessage = new StringJoiner("\n");
        int totalFailures =
                failedTestCaseDetails.values().stream().mapToInt(Integer::intValue).sum();
        failedTestCaseDetails.forEach(
                (detailMessage, detailCount) ->
                        logMessage.add(
                                String.format(
                                        "%d/%d test cases %s. %s",
                                        detailCount,
                                        totalFailures,
                                        testResult.toString(),
                                        detailMessage)));

        return logMessage.toString();
    }

    private String buildTestRunResultsSummary() {
        Map<TestResult, List<AnvilTestCase>> testCasesByResult = groupTestCasesByResult();
        int testCasesSize = testCases.size();

        StringJoiner logMessage = new StringJoiner("\n");
        logMessage.add(
                String.format(
                        "\nTest cases of test run %s by result:",
                        testId != null ? testId : "undefined"));
        testCasesByResult.forEach(
                (testResult, mappedTestCases) -> {
                    logMessage.add(
                            String.format(
                                    "%d/%d test cases %s.",
                                    mappedTestCases.size(), testCasesSize, testResult.toString()));
                });

        return logMessage.toString();
    }

    private String buildTestRunFailureDetailsSummary() {
        Map<TestResult, List<AnvilTestCase>> testCasesByResult = groupTestCasesByResult();
        StringBuilder logMessage = new StringBuilder();

        // log details of failed test cases
        List<TestResult> failureResults = getFailureTestResults();
        if (testCasesByResult.keySet().stream().anyMatch(failureResults::contains)) {
            logMessage.append("\nDetails of failed test cases:\n");
            for (TestResult failureResult : failureResults) {
                if (testCasesByResult.get(failureResult) != null) {
                    logMessage.append(
                            buildTestCaseFailureDetailsSummary(
                                    failureResult, filterUniqueFailedTestCases(failureResult)));
                }
            }
        }

        return logMessage.toString();
    }

    private void logTestRun() {
        MetadataFetcher metadataFetcher = AnvilContext.getInstance().getMetadataFetcher();
        StringBuilder logMessage = new StringBuilder();

        String methodName = getTestMethodName() != null ? getTestMethodName() : "undefined";
        String testIdValue = testId != null ? testId : "undefined";
        String rfcNumber =
                metadataFetcher.getRfcNumber(testId) != null
                        ? metadataFetcher.getRfcNumber(testId).toString()
                        : "undefined";
        String rfcSection =
                metadataFetcher.getRfcSection(testId) != null
                        ? metadataFetcher.getRfcSection(testId)
                        : "undefined";
        String description =
                metadataFetcher.getDescription(testId) != null
                        ? metadataFetcher.getDescription(testId)
                        : "undefined";
        int testCaseSize = testCases.size();

        logMessage.append(
                String.format(
                        "Test Method: %s\n\tTest UID: %s\n\tRFC source: RFC %s, section %s\n\tRFC description: %s\n\tTest cases: %d",
                        methodName, testIdValue, rfcNumber, rfcSection, description, testCaseSize));

        if (!testCases.isEmpty()) {
            logMessage.append(buildTestRunResultsSummary());
            logMessage.append(buildTestRunFailureDetailsSummary());
        }

        LOGGER.info(logMessage.toString());
    }

    @Override
    public String toString() {
        return String.format(
                "AnvilTestRun{displayName = %s.%s, result = %s}",
                testClass != null ? testClass.getName() : "undefined",
                testMethod != null ? testMethod.getName() : "undefined",
                result != null ? result.name() : "undefined");
    }

    public String getTestMethodName() {
        return testClass.getName() + "." + testMethod.getName();
    }

    public AnvilTestRun(ExtensionContext extensionContext) {
        this.uniqueId = extensionContext.getUniqueId();
        this.testClass =
                Utils.getTemplateContainerExtensionContext(extensionContext).getRequiredTestClass();
        this.testMethod =
                Utils.getTemplateContainerExtensionContext(extensionContext)
                        .getTestMethod()
                        .orElseThrow();
        NonCombinatorialAnvilTest nonCombinatorialAnvilTest =
                this.testMethod.getAnnotation(NonCombinatorialAnvilTest.class);
        AnvilTest anvilTest = this.testMethod.getAnnotation(AnvilTest.class);
        this.testId = getTestMethodName(); // fallback
        if (nonCombinatorialAnvilTest != null && !nonCombinatorialAnvilTest.id().isEmpty()) {
            testId = nonCombinatorialAnvilTest.id();
        } else if (anvilTest != null && !anvilTest.id().isEmpty()) {
            testId = anvilTest.id();
        }
        this.scoreContainer = new ScoreContainer(testId);
    }

    public static synchronized AnvilTestRun forExtensionContext(ExtensionContext extensionContext) {
        ExtensionContext resolvedContext =
                Utils.getTemplateContainerExtensionContext(extensionContext);

        if (AnvilContext.getInstance().getTestResult(resolvedContext.getUniqueId()) != null) {
            return AnvilContext.getInstance().getTestResult(resolvedContext.getUniqueId());
        }

        AnvilTestRun container = new AnvilTestRun(resolvedContext);
        AnvilContext.getInstance().addActiveTestRun(container);
        return container;
    }

    public void setResultRaw(int resultRaw) {
        this.resultRaw = resultRaw;
        result = TestResult.resultForBitmask(resultRaw);
    }

    public void finish() {
        finished = true;
        if (result == null) {
            // only determine result automatically if it has not been set before
            result = resolveFinalResult();
        }
        scoreContainer.updateForResult(result);
        AnvilContext.getInstance()
                .addTestResult(result, testClass.getName() + "." + testMethod.getName());
        AnvilContext.getInstance().getMapper().saveTestRunToPath(this);
        AnvilContext.getInstance().testFinished(uniqueId);

        if (result == TestResult.DISABLED) {
            LOGGER.info(
                    "{} is disabled because {}",
                    getTestMethodName() != null ? getTestMethodName() : "undefined",
                    disabledReason != null ? disabledReason : "undefined");
        } else {
            logTestRun();
        }
    }

    public TestResult resolveFinalResult() {
        Set<TestResult> uniqueResultTypes =
                testCases.stream().map(AnvilTestCase::getTestResult).collect(Collectors.toSet());
        if (uniqueResultTypes.contains(TestResult.FULLY_FAILED)
                || uniqueResultTypes.contains(TestResult.PARTIALLY_FAILED)) {
            if (uniqueResultTypes.size() > 1) {
                return TestResult.PARTIALLY_FAILED;
            } else {
                return TestResult.FULLY_FAILED;
            }
        }

        if (uniqueResultTypes.contains(TestResult.CONCEPTUALLY_SUCCEEDED)) {
            return TestResult.CONCEPTUALLY_SUCCEEDED;
        }

        if (uniqueResultTypes.contains(TestResult.STRICTLY_SUCCEEDED)
                && uniqueResultTypes.size() == 1) {
            return TestResult.STRICTLY_SUCCEEDED;
        }

        throw new RuntimeException(
                "Failed to determine final result. Unique results obtained: "
                        + uniqueResultTypes.toString());
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    public String getTestId() {
        return testId;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public void setTestClass(Class<?> testClass) {
        this.testClass = testClass;
    }

    public boolean isFinished() {
        return finished;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getResultRaw() {
        return resultRaw;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public List<AnvilTestCase> getTestCases() {
        return testCases;
    }

    public TestResult getResult() {
        return result;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public List<ParameterCombination> getFailureInducingCombinations() {
        return failureInducingCombinations;
    }

    public ScoreContainer getScoreContainer() {
        return scoreContainer;
    }

    public void add(AnvilTestCase testState) {
        testState.setAssociatedContainer(this);
        this.testCases.add(testState);
    }

    public void setFailureInducingCombinations(
            List<ParameterCombination> failureInducingCombinations) {
        this.failureInducingCombinations = failureInducingCombinations;
    }

    /**
     * @return the readyForCompletion
     */
    public boolean isReadyForCompletion() {
        return readyForCompletion;
    }

    /**
     * @param readyForCompletion the readyForCompletion to set
     */
    public void setReadyForCompletion(boolean readyForCompletion) {
        this.readyForCompletion = readyForCompletion;
    }
}
