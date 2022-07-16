package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.model.config.ConfigContainer;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.Combination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ParameterCombination {
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<DerivationParameter> parameters;
    private DerivationScope derivationScope;

    public ParameterCombination(List<DerivationParameter> parameters) {
        this.parameters = parameters;
    }

    public ParameterCombination(List<DerivationParameter> parameters, DerivationScope derivationScope) {
        this.parameters = parameters;
        this.derivationScope = derivationScope;
    }

    public static ParameterCombination fromCombination(Combination combination) {
        List<DerivationParameter> parameters = new ArrayList<>();
        combination.getParameterValueMap().keySet().forEach(key -> {
            Object obj = combination.getParameterValueMap().get(key).get();
            if (obj instanceof DerivationParameter) {
                parameters.add((DerivationParameter) obj);
            } else {
                LOGGER.warn("Unsupported parameter type ignored");
            }
        });
        return new ParameterCombination(parameters);
    }

    public static ParameterCombination fromArgumentsAccessor(ArgumentsAccessor argumentsAccessor, DerivationScope derivationScope) {
        List<DerivationParameter> parameters = new ArrayList<>();
        for (Object obj : argumentsAccessor.toList()) {
            if (obj instanceof DerivationParameter) {
                parameters.add((DerivationParameter) obj);
            } else {
                LOGGER.warn("Unsupported parameter type ignored");
            }
        }
        return new ParameterCombination(parameters, derivationScope);
    }

    public DerivationParameter getParameter(ParameterIdentifier parameterIdentifier) {
        for (DerivationParameter parameter : parameters) {
            if (parameter.getParameterIdentifier().equals(parameterIdentifier)) {
                return parameter;
            }
        }
        return null;
    }

    public void applyToConfig(ConfigContainer configContainer) {
        for (DerivationParameter parameter : parameters) {
            if (!derivationScope.getManualConfigTypes().contains(parameter.getParameterIdentifier())) {
                parameter.preProcessConfig(configContainer.getConfig(parameter.getConfigClass()), derivationScope);
            }
        }
        for (DerivationParameter parameter : parameters) {
            if (!derivationScope.getManualConfigTypes().contains(parameter.getParameterIdentifier())) {
                parameter.applyToConfig(configContainer.getConfig(parameter.getConfigClass()), derivationScope);
            }
        }
        for (DerivationParameter parameter : parameters) {
            if (!derivationScope.getManualConfigTypes().contains(parameter.getParameterIdentifier())) {
                parameter.postProcessConfig(configContainer.getConfig(parameter.getConfigClass()), derivationScope);
            }
        }
    }

    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (DerivationParameter derivationParameter : parameters) {
            joiner.add(derivationParameter.toString());
        }
        return joiner.toString();
    }
}
