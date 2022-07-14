package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;

import java.util.Collections;
import java.util.List;

public abstract class ModelBasedIpmFactory {

    protected abstract List<ParameterIdentifier> getAllParameterIdentifiers(DerivationScope derivationScope);

    public List<ParameterIdentifier> getModelParameterIdentifiers(DerivationScope derivationScope) {
        ModelType modelType = derivationScope.getModelType();
        if (modelType == DefaultModelType.ALL_PARAMETERS) {
            return getAllParameterIdentifiers(derivationScope);
        }
        return Collections.emptyList();
    }
}
