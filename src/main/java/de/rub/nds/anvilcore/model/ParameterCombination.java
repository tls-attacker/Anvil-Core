/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model;

import com.fasterxml.jackson.annotation.JsonValue;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.Combination;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;

public class ParameterCombination {
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<DerivationParameter> parameterValues;
    private DerivationScope derivationScope;

    public ParameterCombination(List<DerivationParameter> parameters) {
        this.parameterValues = parameters;
    }

    public ParameterCombination(
            List<DerivationParameter> parameters, DerivationScope derivationScope) {
        this.parameterValues = parameters;
        this.derivationScope = derivationScope;
    }

    public static ParameterCombination fromCombination(Combination combination) {
        return fromCombination(combination, null);
    }

    public static ParameterCombination fromCombination(
            Combination combination, DerivationScope derivationScope) {
        List<DerivationParameter> parameters = new ArrayList<>();
        combination
                .getParameterValueMap()
                .keySet()
                .forEach(
                        key -> {
                            Object obj = combination.getParameterValueMap().get(key).get();
                            if (obj instanceof DerivationParameter) {
                                parameters.add((DerivationParameter) obj);
                            } else {
                                LOGGER.warn("Unsupported parameter type ignored");
                            }
                        });
        return new ParameterCombination(parameters, derivationScope);
    }

    public static ParameterCombination fromArgumentsAccessor(
            ArgumentsAccessor argumentsAccessor, DerivationScope derivationScope) {
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
        for (DerivationParameter parameter : getParameterValues()) {
            if (parameter.getParameterIdentifier().equals(parameterIdentifier)) {
                return parameter;
            }
        }
        throw new NoSuchElementException(
                "Found no Parameter for requested identifier " + parameterIdentifier);
    }

    public boolean hasParameter(ParameterIdentifier parameterIdentifier) {
        return getParameterValues().stream()
                .map(DerivationParameter::getParameterIdentifier)
                .anyMatch(parameterIdentifier::equals);
    }

    public boolean hasParameter(Class<?> clazz) {
        return getParameterValues().stream().map(Object::getClass).anyMatch(clazz::equals);
    }

    public <T extends DerivationParameter<?, ?>> T getParameter(Class<T> clazz) {
        T matchingFound = null;
        for (DerivationParameter listed : getParameterValues()) {
            if (clazz.equals(listed.getClass())) {
                if (matchingFound == null) {
                    matchingFound = (T) listed;
                } else {
                    throw new IllegalArgumentException(
                            "Found multiple ParameterIdentifiers for "
                                    + clazz
                                    + ". Full ParameterIdentifier is required.");
                }
            }
        }

        if (matchingFound == null) {
            throw new NoSuchElementException("Found no Parameter for requested class " + clazz);
        }
        return matchingFound;
    }

    public DerivationParameter getLinkedParameter(ParameterIdentifier identifierWithRef) {
        if (!identifierWithRef.hasLinkedParameterIdentifier()) {
            throw new IllegalArgumentException(
                    "Provided ParameterIdentifier has no linked parameter");
        }
        for (DerivationParameter parameter : getParameterValues()) {
            if (parameter
                    .getParameterIdentifier()
                    .equals(identifierWithRef.getLinkedParameterIdentifier())) {
                return parameter;
            }
        }
        return null;
    }

    public void applyToConfig(Object config) {
        for (DerivationParameter parameter : getParameterValues()) {
            if (!derivationScope
                    .getManualConfigTypes()
                    .contains(parameter.getParameterIdentifier())) {
                parameter.preProcessConfig(config, getDerivationScope());
            }
        }
        for (DerivationParameter parameter : getParameterValues()) {
            if (!derivationScope
                    .getManualConfigTypes()
                    .contains(parameter.getParameterIdentifier())) {
                parameter.applyToConfig(config, getDerivationScope());
            }
        }
        for (DerivationParameter parameter : getParameterValues()) {
            if (!derivationScope
                    .getManualConfigTypes()
                    .contains(parameter.getParameterIdentifier())) {
                parameter.postProcessConfig(config, getDerivationScope());
            }
        }
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (DerivationParameter derivationParameter : getParameterValues()) {
            joiner.add(derivationParameter.toString());
        }
        return joiner.toString();
    }

    /**
     * @return the parameterValues
     */
    public List<DerivationParameter> getParameterValues() {
        return Collections.unmodifiableList(parameterValues);
    }

    /**
     * @return the derivationScope
     */
    public DerivationScope getDerivationScope() {
        return derivationScope;
    }

    public void setDerivationScope(DerivationScope derivationScope) {
        this.derivationScope = derivationScope;
    }

    @JsonValue
    public Map<String, Object> jsonObject() {
        Map<String, Object> res = new HashMap<>();
        for (DerivationParameter i : getParameterValues()) {
            res.put(i.getParameterIdentifier().name(), i.getSelectedValue());
        }
        return res;
    }
}
