/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.execution.PcapCapturingInvocationInterceptor;
import de.rub.nds.anvilcore.junit.extension.AnvilTestWatcher;
import de.rub.nds.anvilcore.junit.extension.EndpointConditionExtension;
import de.rub.nds.anvilcore.junit.extension.MethodConditionExtension;
import de.rub.nds.anvilcore.junit.extension.ValueConstraintsConditionExtension;
import de.rub.nds.anvilcore.model.DerivationScope;
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
    ExtensionContextParameterResolver.class,
    PcapCapturingInvocationInterceptor.class
})
public abstract class AnvilTestBaseClass {
    protected static final Logger LOGGER = LogManager.getLogger();

    protected ExtensionContext extensionContext;

    @BeforeEach
    public void setExtensionContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    protected ParameterCombination resolveParameterCombination(
            ArgumentsAccessor argumentsAccessor) {
        return ParameterCombination.fromArgumentsAccessor(
                argumentsAccessor, DerivationScope.fromExtensionContext(extensionContext));
    }

    protected ParameterCombination resolveParameterCombination(
            ArgumentsAccessor argumentsAccessor, DerivationScope derivationScope) {
        return ParameterCombination.fromArgumentsAccessor(argumentsAccessor, derivationScope);
    }
}
