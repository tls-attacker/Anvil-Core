package de.rub.nds.anvilcore.coffee4j.junit;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import de.rub.nds.anvilcore.annotation.AnvilTest;
import de.rwth.swc.coffee4j.junit.*;
import de.rwth.swc.coffee4j.junit.provider.configuration.ConfigurationLoader;
import de.rwth.swc.coffee4j.junit.provider.model.ModelLoader;
import de.rwth.swc.coffee4j.model.Combination;
import de.rwth.swc.coffee4j.model.InputParameterModel;
import de.rwth.swc.coffee4j.model.manager.CombinatorialTestConsumerManager;
import de.rwth.swc.coffee4j.model.manager.CombinatorialTestConsumerManagerConfiguration;
import java.lang.reflect.Method;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.platform.commons.util.Preconditions;

public class AnvilCombinatorialTestExtension extends CombinatorialTestExtension {

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        if (extensionContext.getTestMethod().isEmpty()) {
            return false;
        }
        final Method testMethod = extensionContext.getRequiredTestMethod();
        if (!isAnnotated(testMethod, AnvilTest.class)) {
            return false;
        }
        return CombinatorialTestMethodContext.checkAggregatorOrder(testMethod);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
            ExtensionContext extensionContext) {
        final Method testMethod = extensionContext.getRequiredTestMethod();
        final CombinatorialTestConsumerManagerConfiguration configuration =
                new ConfigurationLoader().load(extensionContext);
        final TestInputIterator iterator = new TestInputIterator(extensionContext);
        final InputParameterModel model = new ModelLoader().load(extensionContext);
        final CombinatorialTestConsumerManager manager =
                new CombinatorialTestConsumerManager(configuration, iterator::add, model);

        diagnoseConstraints(configuration, manager);

        manager.generateInitialTests();

        CombinatorialTestExtension.getStore(extensionContext)
                .put(
                        CombinatorialTestExecutionCallback.REPORTERS_KEY,
                        configuration.getExecutionReporters());
        CombinatorialTestExtension.getStore(extensionContext)
                .put(CombinatorialTestExecutionCallback.MANAGER_KEY, manager);

        final CombinatorialTestMethodContext methodContext =
                new CombinatorialTestMethodContext(testMethod, model);
        final CombinatorialTestNameFormatter nameFormatter =
                new CombinatorialTestNameFormatter("[{index}] {combination}");

        Preconditions.condition(iterator.hasNext(), "Error: no test inputs were generated!");
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .map(testInput -> createInvocationContext(nameFormatter, methodContext, testInput));
    }

    private void diagnoseConstraints(
            CombinatorialTestConsumerManagerConfiguration configuration,
            CombinatorialTestConsumerManager manager) {
        if (configuration.getConflictDetectionConfiguration().isConflictDetectionEnabled()) {
            final boolean isConflictFree = manager.checkConstraintsForConflicts();

            if (configuration.getConflictDetectionConfiguration().shouldAbort()) {
                Preconditions.condition(
                        isConflictFree, "Error: conflicts among constraints detected");
            }
        }
    }

    private TestTemplateInvocationContext createInvocationContext(
            CombinatorialTestNameFormatter nameFormatter,
            CombinatorialTestMethodContext methodContext,
            Combination testInput) {
        return new CombinatorialTestInvocationContext(nameFormatter, methodContext, testInput);
    }
}
