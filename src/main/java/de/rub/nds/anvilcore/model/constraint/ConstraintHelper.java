package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;

import java.util.List;

public class ConstraintHelper {
    public static DerivationParameter getParameterValue(List<DerivationParameter> parameterValues, ParameterIdentifier parameterIdentifier) {
        return parameterValues.stream()
                .filter(v -> v.getParameterIdentifier().equals(parameterIdentifier))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Parameter with identifier " + parameterIdentifier + "was not found"));
    }
}
