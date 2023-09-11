package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.Collections;
import java.util.List;

public abstract class ParameterIdentifierProvider {

    private static List<ParameterIdentifier> allParameterIdentifiers;

    public static List<ParameterIdentifier> getAllParameterIdentifiers() {
        if (allParameterIdentifiers == null) {
            allParameterIdentifiers =
                    AnvilContext.getInstance()
                            .getParameterIdentifierProvider()
                            .generateAllParameterIdentifiers();
        }
        return allParameterIdentifiers;
    }

    public abstract List<ParameterIdentifier> generateAllParameterIdentifiers();

    public List<ParameterIdentifier> getModelParameterIdentifiers(DerivationScope derivationScope) {
        String modelType = derivationScope.getModelType();
        if (modelType.equals(DefaultModelTypes.ALL_PARAMETERS)) {
            return getAllParameterIdentifiers();
        }
        return Collections.emptyList();
    }
}
