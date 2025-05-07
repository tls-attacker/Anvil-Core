/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.List;

public class ConstraintHelper {
    public static DerivationParameter getParameterValue(
            List<DerivationParameter> parameterValues, ParameterIdentifier parameterIdentifier) {
        return parameterValues.stream()
                .filter(v -> v.getParameterIdentifier().equals(parameterIdentifier))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Parameter with identifier "
                                                + parameterIdentifier
                                                + "was not found"));
    }
}
