/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.Utils;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rub.nds.anvilcore.teststate.reporting.AnvilReport;
import de.rwth.swc.coffee4j.model.Combination;
import de.rwth.swc.coffee4j.model.TestInputGroupContext;
import de.rwth.swc.coffee4j.model.report.ExecutionReporter;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Implements TestWatcher, ExecutionReporter and TestExecutionListener. Gets updates from JUnit
 * tests and sends out actions based on those.
 */
public class AnvilTestWatcher implements TestWatcher, ExecutionReporter, TestExecutionListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private ExtensionContext extensionContext;
    private final Map<String, Long> elapsedTimes = new HashMap<>();

    public AnvilTestWatcher() {}

    public AnvilTestWatcher(ExtensionContext context) {
        extensionContext = context;
    }

    /**
     * TestCase or non-combinatorial test finished successfully.
     *
     * @param extensionContext context of the test template
     */
    @Override
    public synchronized void testSuccessful(ExtensionContext extensionContext) {
        if (AnvilContext.getInstance().isAborted()) {
            return;
        }
        AnvilTestRun testRun =
                AnvilContext.getInstance()
                        .getTestResult(
                                Utils.getTemplateContainerExtensionContext(extensionContext)
                                        .getUniqueId());
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            processNonCombinatorial(testRun, extensionContext, TestResult.STRICTLY_SUCCEEDED, null);
        } else {
            AnvilTestCase testCase = getTestCase(extensionContext, testRun);
            if (testCase == null) {
                LOGGER.error("TestCase sould not be null");
                return;
            }
            if (testCase.getTestResult() == null) {
                // test template did not yield a reason why this test did not succeed
                testCase.setTestResult(TestResult.STRICTLY_SUCCEEDED);
            }

            testCase.finalizeAnvilTestCase();
            if (AnvilContext.getInstance().getListener() != null) {
                AnvilContext.getInstance()
                        .getListener()
                        .onTestCaseFinished(testCase, testRun.getTestId());
            }

            if (testRun.isReadyForCompletion()) {
                testRun.finish();
            }
        }
    }

    private void processNonCombinatorial(
            AnvilTestRun testRun,
            ExtensionContext extensionContext,
            TestResult defaultResult,
            Throwable cause) {
        if (testRun == null) {
            testRun = new AnvilTestRun(extensionContext);
            AnvilContext.getInstance().addActiveTestRun(testRun);
            testRun.setResultRaw(defaultResult.getValue());
        } else {
            testRun.setResultRaw(testRun.resolveFinalResult().getValue());
        }

        if (cause != null) {
            testRun.setFailedReason(cause.toString());
        }

        testRun.finish();
    }

    public AnvilTestCase getTestCase(ExtensionContext extensionContext, AnvilTestRun anvilTestRun) {
        return anvilTestRun.getTestCases().stream()
                .filter(
                        testCase ->
                                testCase.getExtensionContext()
                                        .getUniqueId()
                                        .equals(extensionContext.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * TestCase or non-combinatorial test failed / did not pass.
     *
     * @param extensionContext context of the test template
     */
    @Override
    public synchronized void testFailed(ExtensionContext extensionContext, Throwable cause) {
        if (AnvilContext.getInstance().isAborted()) {
            return;
        }
        if (!(cause instanceof AssertionError)) {
            LOGGER.error(
                    "Test failed without AssertionError {}\n",
                    extensionContext.getDisplayName(),
                    cause);
        }
        AnvilTestRun testRun =
                AnvilContext.getInstance()
                        .getTestResult(
                                Utils.getTemplateContainerExtensionContext(extensionContext)
                                        .getUniqueId());
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            processNonCombinatorial(testRun, extensionContext, TestResult.FULLY_FAILED, cause);
        } else {
            AnvilTestCase testCase = getTestCase(extensionContext, testRun);
            if (testCase == null) {
                LOGGER.error("TestCase should not be null.");
                return;
            }
            if (cause != null
                    && (testCase.getTestResult() == null
                            || testCase.getTestResult() == TestResult.NOT_SPECIFIED)) {
                // default to failed for all thrown exceptions
                testCase.setTestResult(TestResult.FULLY_FAILED);
            }
            testRun.setFailedReason(cause.toString());

            testCase.finalizeAnvilTestCase();
            if (AnvilContext.getInstance().getListener() != null) {
                AnvilContext.getInstance()
                        .getListener()
                        .onTestCaseFinished(testCase, testRun.getTestId());
            }

            if (testRun.isReadyForCompletion()) {
                testRun.finish();
            }
        }
    }

    /**
     * TestRun or non-combinatorial test is skipped due to being disabled.
     *
     * @param extensionContext context of the test template
     */
    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> reason) {
        if (AnvilContext.getInstance().isAborted()) {
            return;
        }
        AnvilTestRun testRun = new AnvilTestRun(extensionContext);
        testRun.setResultRaw(TestResult.DISABLED.getValue());
        testRun.setDisabledReason(reason.orElse("No reason specified"));
        AnvilContext.getInstance().addActiveTestRun(testRun);
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            // simple tests finish immediately
            testRun.finish();
        } else if (testRun.isReadyForCompletion()) {
            testRun.finish();
        }
    }

    /**
     * Coffee4j generated the different TestCases for a combinatorial AnvilTest
     *
     * @param context all important information about one group
     * @param testInputs the initially generated test inputs
     */
    @Override
    public void testInputGroupGenerated(
            TestInputGroupContext context, List<Combination> testInputs) {
        AnvilTestRun testRun = new AnvilTestRun(extensionContext);
        AnvilContext.getInstance().addActiveTestRun(testRun);
        AnvilContext.getInstance()
                .addEndInputGenerationTime(
                        extensionContext.getRequiredTestMethod().toString(), new Date());
        LOGGER.trace(
                "Test Inputs generated for " + extensionContext.getRequiredTestMethod().getName());
    }

    /**
     * Coffee4j analyzed which parameter combinations may lead to failed tests.
     *
     * @param context the context of the group for which fault characterization finished
     * @param failureInducingCombinations all failure-inducing combinations found. The order may or
     *     may not be based on an algorithm internal probability metric of the combinations being
     *     failure-inducing
     */
    @Override
    public void faultCharacterizationFinished(
            TestInputGroupContext context, List<Combination> failureInducingCombinations) {
        List<ParameterCombination> failureInducing = new LinkedList<>();
        failureInducingCombinations.forEach(
                combination ->
                        failureInducing.add(ParameterCombination.fromCombination(combination)));
        AnvilTestRun.forExtensionContext(extensionContext)
                .setFailureInducingCombinations(failureInducing);
    }

    /**
     * All TestCases of one TestRun finished.
     *
     * @param context the context of the group which was finished
     */
    @Override
    public void testInputGroupFinished(TestInputGroupContext context) {
        AnvilTestRun.forExtensionContext(extensionContext).setReadyForCompletion(true);
    }

    /**
     * JUnit launcher started testing.
     *
     * @param testPlan information about the discovered tests
     */
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        AnvilContext.getInstance().setTestStartTime(new Date());
        LOGGER.trace("Started execution of " + testPlan.toString());
        if (AnvilContext.getInstance().getListener() != null) {
            AnvilContext.getInstance().getListener().onStarted();
        }
    }

    /**
     * JUnit launcher finished the execution of all tests it discovered.
     *
     * @param testPlan information about the discovered tests
     */
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        LOGGER.trace("Execution of " + testPlan.toString() + " finished");
        AnvilReport anvilReport = new AnvilReport(AnvilContext.getInstance(), false);
        AnvilContext.getInstance().getMapper().saveReportToPath(anvilReport);
        if (AnvilContext.getInstance().getListener() != null) {
            AnvilContext.getInstance().getListener().onReportFinished(anvilReport);
        }
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has been skipped e.g. the test
     * is disabled. Only used for logging.
     *
     * @param testIdentifier may represent a test or a container
     * @param reason reason why it is skipped
     */
    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        LOGGER.trace(testIdentifier.getDisplayName() + " skipped, due to " + reason);
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan is about to be started. Called
     * by TestCases and TestRuns as well as non-combinatorial tests. Only used for logging.
     *
     * @param testIdentifier may represent a test or a container
     */
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        LOGGER.trace(testIdentifier.getDisplayName() + " started");
        if (testIdentifier.isContainer()) {
            elapsedTimes.put(testIdentifier.getUniqueId(), time);
        }
        TestSource source = testIdentifier.getSource().orElse(null);
        if (testIdentifier.isContainer() && source instanceof MethodSource) {
            MethodSource methodSource = (MethodSource) source;
            AnvilContext anvilContext = AnvilContext.getInstance();
            anvilContext.addStartInputGenerationTime(methodSource.getJavaMethod().toString(), date);
        }
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has finished, regardless of
     * the outcome. Called by TestCases and TestRuns as well as non-combinatorial tests. Only used
     * for logging.
     *
     * @param testIdentifier represents a test or a container
     * @param testExecutionResult result of the execution for the supplied TestIdentifier
     */
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
            LOGGER.info(
                    "Internal exception during execution of test container created for test {}. Exception: ",
                    testIdentifier.getDisplayName(),
                    testExecutionResult.getThrowable().get());
        } else {
            LOGGER.trace(testIdentifier.getDisplayName() + " finished");
        }
    }
}
