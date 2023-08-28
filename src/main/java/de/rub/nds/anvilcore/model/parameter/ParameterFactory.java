/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;

public abstract class ParameterFactory {
    protected abstract DerivationParameter getInstance(ParameterIdentifier parameterIdentifier);

    public abstract ParameterScope resolveParameterScope(String scope);

    public static DerivationParameter getInstanceFromIdentifier(
            ParameterIdentifier parameterIdentifier) {
        ParameterFactory parameterFactory =
                AnvilFactoryRegistry.get()
                        .getParameterFactory(parameterIdentifier.getParameterType());
        DerivationParameter newParameter = parameterFactory.getInstance(parameterIdentifier);
        if (!newParameter.getParameterIdentifier().hasLinkedParameterIdentifier()
                && parameterIdentifier.hasLinkedParameterIdentifier()) {
            newParameter
                    .getParameterIdentifier()
                    .setLinkedParameterIdentifier(
                            parameterIdentifier.getLinkedParameterIdentifier());
        }
        return newParameter;
    }
}
