package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.ParameterCombination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;

@ExtendWith(ExtensionContextParameterResolver.class)
public abstract class CombinatorialAnvilTest {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected ExtensionContext extensionContext;

    @BeforeEach
    public void setExtensionContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    protected ParameterCombination resolveParameterCombination(ArgumentsAccessor argumentsAccessor) {
        return ParameterCombination.fromArgumentsAccessor(argumentsAccessor, new DerivationScope(extensionContext));
    }

    protected ParameterCombination resolveParameterCombination(ArgumentsAccessor argumentsAccessor, DerivationScope derivationScope) {
        return ParameterCombination.fromArgumentsAccessor(argumentsAccessor, derivationScope);
    }
}
