/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilContextRegistry;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.Collections;
import java.util.List;

public abstract class ParameterIdentifierProvider {

    private static List<ParameterIdentifier> allParameterIdentifiers;

    public static List<ParameterIdentifier> getAllParameterIdentifiers(AnvilContext context) {
        if (allParameterIdentifiers == null) {
            allParameterIdentifiers =
                    context.getParameterIdentifierProvider()
                            .generateAllParameterIdentifiers(context.getContextId());
        }
        return allParameterIdentifiers;
    }

    public abstract List<ParameterIdentifier> generateAllParameterIdentifiers(
            String anvilContextId);

    public List<ParameterIdentifier> getModelParameterIdentifiers(DerivationScope derivationScope) {
        String modelType = derivationScope.getModelType();
        if (modelType.equals(DefaultModelTypes.ALL_PARAMETERS)) {
            return getAllParameterIdentifiers(
                    AnvilContextRegistry.byExtensionContext(derivationScope.getExtensionContext()));
        }
        return Collections.emptyList();
    }
}
