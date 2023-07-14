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

        if (!Utils.extensionContextIsBasedOnCombinatorialTesting(
                extensionContext.getParent().get())) {
            // AnvilTestStateContainer is never called for simple tests
            testStateContainer.finished();
        }
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

        String uniqueId =
                Utils.getTemplateContainerExtensionContext(extensionContext).getUniqueId();
        AnvilTestStateContainer testStateContainer =
                createResult(extensionContext, TestResult.FULLY_FAILED);
        if (testStateContainer == null) {
            if (AnvilContext.getInstance().testIsFinished(uniqueId)) {
                return;
            }
            LOGGER.error(
                    "Illegal state: AnnotatedStateContainer is null even though test is not finished yet");
        }

        AnvilTestState testState =
                testStateContainer.getStates().stream()
                        .filter(
                                state ->
                                        state.getExtensionContext()
                                                .getUniqueId()
                                                .equals(extensionContext.getUniqueId()))
                        .findFirst()
                        .orElse(null);

        if (testState == null) {
            if (Utils.extensionContextIsBasedOnCombinatorialTesting(
                    extensionContext.getParent().get())) {
                testState = new AnvilTestState(null, extensionContext);
                testState.setFailedReason(cause);
            } else {
                // AnvilTestStateContainer is never called for simple tests
                testStateContainer.setFailedReason(cause.toString());
                testStateContainer.finished();
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
            testStateContainer.finished();
        }
    }
}
