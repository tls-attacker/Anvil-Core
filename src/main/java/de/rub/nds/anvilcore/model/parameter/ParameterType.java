package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.TestContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ParameterType {
     static ParameterType resolveParameterType(String type) {
         Map<ParameterType, ParameterFactory> knownParameters = TestContext.getInstance().getKnownParameters();
         List<ParameterType> result = knownParameters.keySet().stream()
                .filter(parameterType -> parameterType.toString().equals(type))
                .collect(Collectors.toList());
         if (result.size() == 0) {
            throw new IllegalArgumentException("Parameter " + type + " is not known");
         }
         return result.get(0);
    }
}
