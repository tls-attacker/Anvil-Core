package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.model.ParameterCombination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@ExtendWith(ExtensionContextParameterResolver.class)
public abstract class CombinatorialAnvilTest {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected ExtensionContext extensionContext;
    protected ParameterCombination parameterCombination;

    @BeforeEach
    public void setExtensionContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }
}
