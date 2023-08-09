package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.junit.extension.AnvilTestWatcher;
import de.rub.nds.anvilcore.junit.extension.EndpointConditionExtension;
import de.rub.nds.anvilcore.junit.extension.MethodConditionExtension;
import de.rub.nds.anvilcore.junit.extension.ValueConstraintsConditionExtension;
import de.rub.nds.anvilcore.model.AnvilTestTemplate;
import de.rub.nds.anvilcore.model.ParameterCombination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;

@ExtendWith({
    AnvilTestWatcher.class,
    EndpointConditionExtension.class,
    MethodConditionExtension.class,
    ValueConstraintsConditionExtension.class,
    ExtensionContextParameterResolver.class
})
public abstract class CombinatorialAnvilTest {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected ExtensionContext extensionContext;

    @BeforeEach
    public void setExtensionContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    protected ParameterCombination resolveParameterCombination(
            ArgumentsAccessor argumentsAccessor) {
        return ParameterCombination.fromArgumentsAccessor(
                argumentsAccessor, new AnvilTestTemplate(extensionContext));
    }

    protected ParameterCombination resolveParameterCombination(
            ArgumentsAccessor argumentsAccessor, AnvilTestTemplate anvilTestTemplate) {
        return ParameterCombination.fromArgumentsAccessor(argumentsAccessor, anvilTestTemplate);
    }
}
