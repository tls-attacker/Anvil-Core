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
