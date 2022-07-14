package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.context.AnvilContext;

import java.util.List;
import java.util.stream.Collectors;

public interface ModelType {
    static ModelType resolveModelType(String type) {
        List<ModelType> knownModelTypes = AnvilContext.getInstance().getKnownModelTypes();
        List<ModelType> result = knownModelTypes.stream()
                .filter(modelType -> modelType.toString().toLowerCase().equals(type))
                .collect(Collectors.toList());
        if (result.size() == 0) {
            throw new IllegalArgumentException("ModelType " + type + " is not known");
        }
        return result.get(0);
    }
}
