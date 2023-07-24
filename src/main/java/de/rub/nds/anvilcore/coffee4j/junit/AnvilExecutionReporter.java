package de.rub.nds.anvilcore.coffee4j.junit;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
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
        AnvilTestRun testStateContainer = new AnvilTestRun(extensionContext);
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
        AnvilTestRun.forExtensionContext(extensionContext)
                .setFailureInducingCombinations(failureInducing);
    }

    @Override
    public void testInputGroupFinished(TestInputGroupContext context) {
        AnvilTestRun.forExtensionContext(extensionContext).setReadyForCompletion(true);
    }
}
