/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.Collections;
import java.util.List;

public abstract class ParameterIdentifierProvider {

    public abstract List<ParameterIdentifier> getAllParameterIdentifiers();

    public List<ParameterIdentifier> getModelParameterIdentifiers(DerivationScope derivationScope) {
        ModelType modelType = derivationScope.getModelType();
        if (modelType == DefaultModelType.ALL_PARAMETERS) {
            return getAllParameterIdentifiers();
        }
        return Collections.emptyList();
    }
}
