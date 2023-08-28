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
import java.util.List;
import java.util.stream.Collectors;

public interface ModelType {
    static ModelType resolveModelType(String type) {
        List<ModelType> knownModelTypes = AnvilContext.getInstance().getKnownModelTypes();
        List<ModelType> result =
                knownModelTypes.stream()
                        .filter(
                                modelType ->
                                        modelType
                                                .toString()
                                                .toLowerCase()
                                                .equals(type.toLowerCase()))
                        .collect(Collectors.toList());
        if (result.size() == 0) {
            throw new IllegalArgumentException("ModelType " + type + " is not known");
        } else if (result.size() > 1) {
            throw new IllegalArgumentException("Found conflicting ModelType names for " + type);
        }
        return result.get(0);
    }
}
