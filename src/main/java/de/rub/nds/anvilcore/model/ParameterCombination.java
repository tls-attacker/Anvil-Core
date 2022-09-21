package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.model.config.AnvilConfig;
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

    private final List<DerivationParameter> parameterValues;
    private DerivationScope derivationScope;

    public ParameterCombination(List<DerivationParameter> parameters) {
        this.parameterValues = parameters;
    }

    public ParameterCombination(List<DerivationParameter> parameters, DerivationScope derivationScope) {
        this.parameterValues = parameters;
        this.parameterValues.addAll(IpmProvider.getStaticParameterValues(derivationScope));
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
        for (DerivationParameter parameter : parameterValues) {
            if (parameter.getParameterIdentifier().equals(parameterIdentifier)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Retrieve the parameters for this combination.
     *
     * @return a list of parameters and their selected values
     */
    public List<DerivationParameter> getParameters() {
        return this.parameterValues;
    }

    public void applyToConfig(AnvilConfig config) {
        for (DerivationParameter parameter : parameterValues) {
            if (!derivationScope.getManualConfigTypes().contains(parameter.getParameterIdentifier())) {
                parameter.preProcessConfig(config, derivationScope);
            }
        }
        for (DerivationParameter parameter : parameterValues) {
            if (!derivationScope.getManualConfigTypes().contains(parameter.getParameterIdentifier())) {
                parameter.applyToConfig(config, derivationScope);
            }
        }
        for (DerivationParameter parameter : parameterValues) {
            if (!derivationScope.getManualConfigTypes().contains(parameter.getParameterIdentifier())) {
                parameter.postProcessConfig(config, derivationScope);
            }
        }
    }

    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (DerivationParameter derivationParameter : parameterValues) {
            joiner.add(derivationParameter.toString());
        }
        return joiner.toString();
    }
}
