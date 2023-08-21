package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.Utils;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rwth.swc.coffee4j.model.Combination;
import de.rwth.swc.coffee4j.model.TestInputGroupContext;
import de.rwth.swc.coffee4j.model.report.ExecutionReporter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class AnvilTestWatcher implements TestWatcher, ExecutionReporter {
    private static final Logger LOGGER = LogManager.getLogger();

    private ExtensionContext extensionContext;

    public AnvilTestWatcher() {}

    public AnvilTestWatcher(ExtensionContext context) {
        extensionContext = context;
    }

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
            if (testCase != null && testCase.getTestResult() == null) {
                // test template did not yield a reason why this test did not succeed
                testCase.setTestResult(TestResult.STRICTLY_SUCCEEDED);
            }

            if (AnvilContext.getInstance().getListener() != null) {
                AnvilContext.getInstance()
                        .getListener()
                        .onTestCaseFinished(
                                testCase,
                                testRun.getTestClass().getName(),
                                testRun.getTestMethod().getName());
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

    private AnvilTestCase getTestCase(
            ExtensionContext extensionContext, AnvilTestRun anvilTestRun) {
        return anvilTestRun.getTestCases().stream()
                .filter(
                        testCase ->
                                testCase.getExtensionContext()
                                        .getUniqueId()
                                        .equals(extensionContext.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

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
            if (testCase != null && cause != null && testCase.getTestResult() == null) {
                // default to failed for all thrown exceptions
                testCase.setTestResult(TestResult.FULLY_FAILED);
            }
            testRun.setFailedReason(cause.toString());

            if (AnvilContext.getInstance().getListener() != null) {
                AnvilContext.getInstance()
                        .getListener()
                        .onTestCaseFinished(
                                testCase,
                                testRun.getTestClass().getName(),
                                testRun.getTestMethod().getName());
            }

            if (testRun.isReadyForCompletion()) {
                testRun.finish();
            }
        }
    }

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

    @Override
    public void testInputGroupGenerated(
            TestInputGroupContext context, List<Combination> testInputs) {
        AnvilTestRun testRun = new AnvilTestRun(extensionContext);
        AnvilContext.getInstance().addActiveTestRun(testRun);
        LOGGER.trace(
                "Test Inputs generated for " + extensionContext.getRequiredTestMethod().getName());
    }

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

    @Override
    public void testInputGroupFinished(TestInputGroupContext context) {
        AnvilTestRun.forExtensionContext(extensionContext).setReadyForCompletion(true);
    }
}
