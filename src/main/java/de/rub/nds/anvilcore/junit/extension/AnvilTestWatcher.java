package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.Utils;
import de.rub.nds.anvilcore.teststate.AnvilTestState;
import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;
import de.rub.nds.anvilcore.teststate.TestResult;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class AnvilTestWatcher implements TestWatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    private AnvilTestStateContainer createResult(
            ExtensionContext extensionContext, TestResult testResult) {
        String uniqueId =
                Utils.getTemplateContainerExtensionContext(extensionContext).getUniqueId();
        AnvilTestStateContainer testStateContainer =
                AnvilContext.getInstance().getTestResult(uniqueId);

        if (testStateContainer != null || AnvilContext.getInstance().testIsFinished(uniqueId)) {
            return testStateContainer;
        }

        testStateContainer = AnvilTestStateContainer.forExtensionContext(extensionContext);
        testStateContainer.setResultRaw(testResult.getValue());
        AnvilContext.getInstance().addTestResult(testStateContainer);
        return testStateContainer;
    }

    @Override
    public synchronized void testSuccessful(ExtensionContext extensionContext) {
        AnvilContext.getInstance().testSucceeded();
        AnvilTestStateContainer testStateContainer =
                createResult(extensionContext, TestResult.STRICTLY_SUCCEEDED);
        AnvilTestState testState = getTestState(extensionContext, testStateContainer);

        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            // Non-combinatorial tests finish immediately
            testStateContainer.finish();
        } else {
            if (testState != null && testState.getTestResult() == null) {
                // test template did not yield a reason why this test did not succeed
                testState.setTestResult(TestResult.STRICTLY_SUCCEEDED);
            }
            if (testStateContainer.isReadyForCompletion()) {
                testStateContainer.finish();
            }
        }
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
                createResult(extensionContext, TestResult.FULLY_FAILED);

        AnvilTestState testState = getTestState(extensionContext, testStateContainer);

        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            // Non-combinatorial tests finish immediately
            testStateContainer.setFailedReason(cause.toString());
            testStateContainer.finish();
        } else {
            if (testState == null) {
                // the test did not register a state yet
                testState = new AnvilTestState(null, extensionContext);
                testStateContainer.add(testState);
            }
            testState.setTestResult(TestResult.FULLY_FAILED);
            testState.setFailedReason(cause);

            if (testStateContainer.isReadyForCompletion()) {
                testStateContainer.finish();
            }
        }
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> reason) {
        AnvilContext.getInstance().testDisabled();
        AnvilTestStateContainer testStateContainer =
                createResult(extensionContext, TestResult.DISABLED);
        testStateContainer.setDisabledReason(reason.orElse("No reason specified"));
        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            // AnvilTestStateContainer is never called for simple tests
            testStateContainer.finish();
        }
    }
}
