package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the properties affected by the test derivation models. This class should be
 * implemented as enum. Its values have to be registered in the {@link AnvilFactoryRegistry} as
 * known parameters.
 */
public interface ParameterType {
    static ParameterType resolveParameterType(String type) {
        Map<ParameterType, ParameterFactory> knownParameters =
                AnvilFactoryRegistry.get().getKnownParameters();
        Optional<ParameterType> result =
                knownParameters.keySet().stream()
                        .filter(parameterType -> parameterType.toString().equalsIgnoreCase(type))
                        .findFirst();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Parameter " + type + " is not known");
        }
        return result.get();
    }
}
