package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.Utils;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.AnvilTestState;
import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;
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
        AnvilContext.getInstance().testSucceeded();
        AnvilTestStateContainer testStateContainer =
                AnvilContext.getInstance()
                        .getTestResult(
                                Utils.getTemplateContainerExtensionContext(extensionContext)
                                        .getUniqueId());
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            processNonCombinatorial(
                    testStateContainer, extensionContext, TestResult.STRICTLY_SUCCEEDED, null);
        } else {
            AnvilTestState testState = getTestState(extensionContext, testStateContainer);
            if (testState != null && testState.getTestResult() == null) {
                // test template did not yield a reason why this test did not succeed
                testState.setTestResult(TestResult.STRICTLY_SUCCEEDED);
            }

            if (testStateContainer.isReadyForCompletion()) {
                testStateContainer.finish();
            }
        }
    }

    private void processNonCombinatorial(
            AnvilTestStateContainer testStateContainer,
            ExtensionContext extensionContext,
            TestResult defaultResult,
            Throwable cause) {
        if (testStateContainer == null) {
            testStateContainer = new AnvilTestStateContainer(extensionContext);
            AnvilContext.getInstance().addTestStateContainer(testStateContainer);
            testStateContainer.setResultRaw(defaultResult.getValue());
        } else {
            testStateContainer.setResultRaw(testStateContainer.resolveFinalResult().getValue());
        }

        if (cause != null) {
            testStateContainer.setFailedReason(cause.toString());
        }

        testStateContainer.finish();
    }

    private AnvilTestState getTestState(
            ExtensionContext extensionContext, AnvilTestStateContainer testStateContainer) {
        return testStateContainer.getStates().stream()
                .filter(
                        state ->
                                state.getExtensionContext()
                                        .getUniqueId()
                                        .equals(extensionContext.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public synchronized void testFailed(ExtensionContext extensionContext, Throwable cause) {
        AnvilContext.getInstance().testFailed();

        if (!(cause instanceof AssertionError)) {
            LOGGER.error(
                    "Test failed without AssertionError {}\n",
                    extensionContext.getDisplayName(),
                    cause);
        }
        AnvilTestStateContainer testStateContainer =
                AnvilContext.getInstance()
                        .getTestResult(
                                Utils.getTemplateContainerExtensionContext(extensionContext)
                                        .getUniqueId());
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            processNonCombinatorial(
                    testStateContainer, extensionContext, TestResult.FULLY_FAILED, cause);
        } else {
            AnvilTestState testState = getTestState(extensionContext, testStateContainer);
            if (testState != null && cause != null && testState.getTestResult() == null) {
                // default to failed for all thrown exceptions
                testState.setTestResult(TestResult.FULLY_FAILED);
            }
            testStateContainer.setFailedReason(cause.toString());

            if (testStateContainer.isReadyForCompletion()) {
                testStateContainer.finish();
            }
        }
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> reason) {
        AnvilContext.getInstance().testDisabled();
        AnvilTestStateContainer testStateContainer = new AnvilTestStateContainer(extensionContext);
        testStateContainer.setResultRaw(TestResult.DISABLED.getValue());
        testStateContainer.setDisabledReason(reason.orElse("No reason specified"));
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            // simple tests finish immediately
            testStateContainer.finish();
        } else if (testStateContainer.isReadyForCompletion()) {
            testStateContainer.finish();
        }
    }

    @Override
    public void testInputGroupGenerated(
            TestInputGroupContext context, List<Combination> testInputs) {
        AnvilTestStateContainer testStateContainer = new AnvilTestStateContainer(extensionContext);
        AnvilContext.getInstance().addTestStateContainer(testStateContainer);
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
        AnvilTestStateContainer.forExtensionContext(extensionContext)
                .setFailureInducingCombinations(failureInducing);
    }

    @Override
    public void testInputGroupFinished(TestInputGroupContext context) {
        AnvilTestStateContainer.forExtensionContext(extensionContext).setReadyForCompletion(true);
    }
}
