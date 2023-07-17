package de.rub.nds.anvilcore.coffee4j.junit;

import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;
import de.rwth.swc.coffee4j.model.Combination;
import de.rwth.swc.coffee4j.model.TestInputGroupContext;
import de.rwth.swc.coffee4j.model.report.ExecutionReporter;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AnvilExecutionReporter implements ExecutionReporter {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ExtensionContext extensionContext;

    public AnvilExecutionReporter(ExtensionContext context) {
        extensionContext = context;
    }

    @Override
    public void testInputGroupGenerated(
            TestInputGroupContext context, List<Combination> testInputs) {
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
