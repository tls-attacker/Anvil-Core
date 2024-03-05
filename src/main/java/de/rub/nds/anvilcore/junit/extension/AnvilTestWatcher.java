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
import de.rub.nds.anvilcore.util.TestIdResolver;
import de.rwth.swc.coffee4j.model.Combination;
import de.rwth.swc.coffee4j.model.TestInputGroupContext;
import de.rwth.swc.coffee4j.model.report.ExecutionReporter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.engine.TestExecutionResult;
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

    // initiated only by ExecutionReporter
    private ExtensionContext extensionContext;
    // for keeping time
    private static final Map<String, Long> executionTimes = new HashMap<>();
    private static final Map<String, Long> generationTimes = new HashMap<>();

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
                        .getTestRun(
                                Utils.getTemplateContainerExtensionContext(extensionContext)
                                        .getUniqueId());
        AnvilTestCase testCase = AnvilTestCase.fromExtensionContext(extensionContext);
        if (testCase != null
                && (testCase.getTestResult() == null
                        || testCase.getTestResult() == TestResult.NOT_SPECIFIED)) {
            // test template did not yield a reason why this test did not succeed
            testCase.setTestResult(TestResult.STRICTLY_SUCCEEDED);
        }

        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            processNonCombinatorial(testRun, extensionContext, TestResult.STRICTLY_SUCCEEDED, null);
        } else {
            if (testCase == null) {
                LOGGER.error("TestCase should not be null");
                return;
            }

            if (AnvilContext.getInstance().getListener() != null) {
                AnvilContext.getInstance()
                        .getListener()
                        .onTestCaseFinished(testCase, testRun.getTestId());
            }

            if (testRun.isReadyForCompletion()) {
                finishAndTime(testRun);
            }
        }
    }

    private void finishAndTime(AnvilTestRun testRun) {
        Long startTime = executionTimes.get(testRun.getUniqueId());
        if (startTime != null) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            testRun.setExecutionTimeMillis(elapsedTime);
        }
        testRun.finish();
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
            if (!(cause instanceof AssertionError)) {
                LOGGER.error(
                        "Test failed without AssertionError {}\n",
                        extensionContext.getDisplayName(),
                        cause);
                testRun.setResultRaw(TestResult.TEST_SUITE_ERROR.getValue());
            }
        }

        finishAndTime(testRun);
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
        AnvilTestRun testRun =
                AnvilContext.getInstance()
                        .getTestRun(
                                Utils.getTemplateContainerExtensionContext(extensionContext)
                                        .getUniqueId());
        AnvilTestCase testCase = AnvilTestCase.fromExtensionContext(extensionContext);
        if (cause != null && testCase != null) {
            if (!(cause instanceof AssertionError)) {
                LOGGER.error(
                        "Test failed without AssertionError {}\n",
                        extensionContext.getDisplayName(),
                        cause);
                testCase.setTestResult(TestResult.TEST_SUITE_ERROR);
            } else if (testCase.getTestResult() == null
                    || testCase.getTestResult() == TestResult.NOT_SPECIFIED) {
                // default to failed for all AssertionErrors
                testCase.setTestResult(TestResult.FULLY_FAILED);
            }
            testRun.setFailedReason(retrieveThrowableReason(cause));
        }

        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            processNonCombinatorial(testRun, extensionContext, TestResult.FULLY_FAILED, cause);
        } else {
            if (testCase == null) {
                LOGGER.error("TestCase should not be null.");
                return;
            }

            if (AnvilContext.getInstance().getListener() != null) {
                AnvilContext.getInstance()
                        .getListener()
                        .onTestCaseFinished(testCase, testRun.getTestId());
            }

            if (testRun.isReadyForCompletion()) {
                finishAndTime(testRun);
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
        AnvilTestRun testRun = AnvilTestRun.forExtensionContext(extensionContext);
        testRun.setResultRaw(TestResult.DISABLED.getValue());
        testRun.setDisabledReason(reason.orElse("No reason specified"));
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
        LOGGER.trace(
                "Test Inputs generated for " + extensionContext.getRequiredTestMethod().getName());

        Long generationStartTime = generationTimes.get(extensionContext.getUniqueId());
        if (generationStartTime != null) {
            long generationTime = System.currentTimeMillis() - generationStartTime;
            generationTimes.put(extensionContext.getUniqueId(), generationTime);
            executionTimes.put(extensionContext.getUniqueId(), System.currentTimeMillis());
            AnvilTestRun.forExtensionContext(extensionContext)
                    .setGenerationTimeMillis(generationTime);
        }
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
        AnvilReport anvilReport = new AnvilReport(AnvilContext.getInstance(), true);
        AnvilContext.getInstance().getMapper().saveReportToPath(anvilReport);
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
        if (!testIdentifier.isContainer()
                && testIdentifier.getSource().isPresent()
                && testIdentifier.getSource().get() instanceof MethodSource) {
            LOGGER.trace(
                    testIdentifier.getDisplayName()
                            + " of test "
                            + TestIdResolver.resolveTestId(
                                    ((MethodSource) testIdentifier.getSource().get())
                                            .getJavaMethod())
                            + " started");
        } else {
            LOGGER.trace(testIdentifier.getDisplayName() + " started");
        }
        if (testIdentifier.isContainer()) {
            generationTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
            executionTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Called when the execution of a leaf or subtree of the TestPlan has finished, regardless of
     * the outcome. Called by TestCases and TestRuns as well as non-combinatorial tests. This is
     * also the only place where we can catch test initialization errors.
     *
     * @param testIdentifier represents a test or a container
     * @param testExecutionResult result of the execution for the supplied TestIdentifier
     */
    @Override
    public void executionFinished(
            TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testExecutionResult.getThrowable().isPresent() && testIdentifier.isContainer()) {
            handleFailedTestInitialization(testIdentifier, testExecutionResult);
        }
        if (!testIdentifier.isContainer()
                && testIdentifier.getSource().isPresent()
                && testIdentifier.getSource().get() instanceof MethodSource) {
            LOGGER.trace(
                    testIdentifier.getDisplayName()
                            + " of test "
                            + TestIdResolver.resolveTestId(
                                    ((MethodSource) testIdentifier.getSource().get())
                                            .getJavaMethod())
                            + " started");
        } else {
            LOGGER.trace(testIdentifier.getDisplayName() + " finished");
        }
        if (testIdentifier.isContainer()) {
            Long startTime = executionTimes.get(testIdentifier.getUniqueId());
            if (startTime != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                executionTimes.put(testIdentifier.getUniqueId(), elapsedTime);
            }
        }
    }
    /**
     * Combinatorial tests might fail before we enter the body of the test template. This is the
     * case when coffee4j fails to produce the test inputs. In this case, no container will be added
     * by the test itself. We need to add it here, so the failure is added to the test report.
     */
    private void handleFailedTestInitialization(
            TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        LOGGER.error(
                "Internal exception during execution of test container created for test {}. Exception: ",
                testIdentifier.getDisplayName(),
                testExecutionResult.getThrowable().get());
        AnvilTestRun testRun = AnvilTestRun.forFailedInitialization(testIdentifier);
        AnvilContext.getInstance().addActiveTestRun(testRun);
        testRun.setResultRaw(TestResult.TEST_SUITE_ERROR.getValue());
        testRun.setFailedReason(retrieveThrowableReason(testExecutionResult.getThrowable().get()));
        // Finalize artificial result immediately
        testRun.setReadyForCompletion(true);
        testRun.finish();
    }

    private String retrieveThrowableReason(Throwable thrown) {
        if (thrown instanceof AssertionError) {
            return thrown.toString();
        } else {
            // add extensive stack trace for all unexpected cases
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            thrown.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
}
