/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rwth.swc.coffee4j.model.Parameter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class DerivationParameter<ConfigType, ValueType> {
    private static final Logger LOGGER = LogManager.getLogger();

    private ValueType selectedValue;
    private final Class<ValueType> valueClass;
    private final Class<ConfigType> configClass;
    private final ParameterIdentifier parameterIdentifier;

    public DerivationParameter(
            Class<ValueType> valueClass,
            Class<ConfigType> configClass,
            ParameterIdentifier parameterIdentifier) {
        this.valueClass = valueClass;
        this.configClass = configClass;
        this.parameterIdentifier = parameterIdentifier;
    }

    public ValueType getSelectedValue() {
        return selectedValue;
    }

    protected void setSelectedValue(ValueType selectedValue) {
        this.selectedValue = selectedValue;
    }

    public Class<ValueType> getValueClass() {
        return valueClass;
    }

    public Class<ConfigType> getConfigClass() {
        return configClass;
    }

    public ParameterIdentifier getParameterIdentifier() {
        return parameterIdentifier;
    }

    public void preProcessConfig(ConfigType config, DerivationScope derivationScope) {}
    ;

    public abstract void applyToConfig(ConfigType config, DerivationScope derivationScope);

    public void postProcessConfig(ConfigType config, DerivationScope derivationScope) {}
    ;

    public abstract List<DerivationParameter<ConfigType, ValueType>> getParameterValues(
            DerivationScope derivationScope);

    public List<DerivationParameter<ConfigType, ValueType>> getConstrainedParameterValues(
            DerivationScope derivationScope) {
        if (derivationScope.hasExplicitValues(parameterIdentifier)) {
            return getExplicitValues(derivationScope);
        } else {
            return getParameterValues(derivationScope).stream()
                    .filter(
                            value ->
                                    valueApplicableUnderAllConstraints(
                                            derivationScope.getExtensionContext(),
                                            derivationScope.getValueConstraints(),
                                            (ValueType) value.getSelectedValue()))
                    .collect(Collectors.toList());
        }
    }

    public List<ConditionalConstraint> getDefaultConditionalConstraints(
            DerivationScope derivationScope) {
        return new ArrayList<>();
    }

    public List<ConditionalConstraint> getConditionalConstraints(DerivationScope derivationScope) {
        if (derivationScope.hasExplicitModelingConstraints(parameterIdentifier)) {
            return getExplicitModelingConstraints(derivationScope);
        } else {
            // return Collections.emptyList();
            return getDefaultConditionalConstraints(derivationScope);
        }
    }

    public Parameter.Builder getParameterBuilder(DerivationScope derivationScope) {
        List<DerivationParameter<ConfigType, ValueType>> parameterValues =
                getConstrainedParameterValues(derivationScope);
        return Parameter.parameter(parameterIdentifier.toString())
                .values(parameterValues.toArray());
    }

    public boolean canBeModeled(DerivationScope derivationScope) {
        return getConstrainedParameterValues(derivationScope).size() > 1;
    }

    public boolean hasNoApplicableValues(DerivationScope derivationScope) {
        return getConstrainedParameterValues(derivationScope).isEmpty();
    }

    protected abstract DerivationParameter<ConfigType, ValueType> generateValue(
            ValueType selectedValue);

    private List<DerivationParameter<ConfigType, ValueType>> getExplicitValues(
            DerivationScope derivationScope) {
        try {
            String methodName = derivationScope.getExplicitValues().get(parameterIdentifier);
            Method method =
                    derivationScope
                            .getExtensionContext()
                            .getRequiredTestClass()
                            .getMethod(methodName, DerivationScope.class);
            Constructor constructor =
                    derivationScope.getExtensionContext().getRequiredTestClass().getConstructor();
            Object instance = constructor.newInstance();

            setExtensionContextIfAvailable(
                    instance,
                    derivationScope.getExtensionContext().getRequiredTestClass(),
                    derivationScope.getExtensionContext());

            return (List<DerivationParameter<ConfigType, ValueType>>)
                    method.invoke(instance, derivationScope);
        } catch (NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException
                | InstantiationException e) {
            LOGGER.error("Was unable to fetch explicit values for type " + parameterIdentifier, e);
            return Collections.emptyList();
        }
    }

    private boolean valueApplicableUnderAllConstraints(
            ExtensionContext extensionContext,
            List<ValueConstraint> valueConstraints,
            ValueType value) {
        for (ValueConstraint constraint : valueConstraints) {
            if (constraint.getAffectedParameter().equals(parameterIdentifier)) {
                if (!valueApplicableUnderConstraint(extensionContext, constraint, value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean valueApplicableUnderConstraint(
            ExtensionContext extensionContext, ValueConstraint constraint, ValueType value) {
        try {
            Method method;
            Constructor constructor;
            if (constraint.isDynamic()) {
                // dynamic - pass value to method of specified class
                method =
                        constraint
                                .getClazz()
                                .getMethod(constraint.getEvaluationMethod(), valueClass);
                constructor = constraint.getClazz().getConstructor();
                Object instance = constructor.newInstance();

                setExtensionContextIfAvailable(instance, constraint.getClazz(), extensionContext);

                return (Boolean) method.invoke(instance, value);
            } else {
                // static - call value.method and use return value
                method = valueClass.getMethod(constraint.getEvaluationMethod());
                return (Boolean) method.invoke(value);
            }
        } catch (InvocationTargetException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException e) {
            LOGGER.error(
                    "Was unable to invoke constraint method for type " + parameterIdentifier, e);
            throw new RuntimeException(e);
        }
    }

    private List<ConditionalConstraint> getExplicitModelingConstraints(
            DerivationScope derivationScope) {
        try {
            String methodName =
                    derivationScope.getExplicitModelingConstraints().get(parameterIdentifier);
            Method method =
                    derivationScope
                            .getExtensionContext()
                            .getRequiredTestClass()
                            .getMethod(methodName, DerivationScope.class);
            Constructor constructor =
                    derivationScope.getExtensionContext().getRequiredTestClass().getConstructor();
            Object instance = constructor.newInstance();

            setExtensionContextIfAvailable(
                    instance,
                    derivationScope.getExtensionContext().getRequiredTestClass(),
                    derivationScope.getExtensionContext());

            return (List<ConditionalConstraint>) method.invoke(instance, derivationScope);
        } catch (NoSuchMethodException
                | InvocationTargetException
                | IllegalArgumentException
                | IllegalAccessException
                | InstantiationException e) {
            LOGGER.error(
                    "Was unable to fetch explicit constraints for type " + parameterIdentifier, e);
            return new ArrayList<>();
        }
    }

    /**
     * Attempts to set the ExtensionContext on an instance by looking for a setExtensionContext
     * method. If the method exists, it invokes it with the provided ExtensionContext. This avoids
     * errors where helper methods don't have access to their respective AnvilContext and other
     * execution-specific context objects.
     *
     * @param instance The instance to set the ExtensionContext on
     * @param clazz The class of the instance
     * @param extensionContext The ExtensionContext to set
     */
    private void setExtensionContextIfAvailable(
            Object instance, Class<?> clazz, ExtensionContext extensionContext) {
        try {
            Method setContextMethod =
                    clazz.getMethod("setExtensionContext", ExtensionContext.class);
            setContextMethod.setAccessible(true);
            setContextMethod.invoke(instance, extensionContext);
        } catch (NoSuchMethodException e) {
            LOGGER.warn(
                    "Class {} does not provide a setExtensionContext method. ExtensionContext will not be available to the instance.",
                    clazz.getName());
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Failed to invoke setExtensionContext on class {}", clazz.getName(), e);
        }
    }

    @Override
    public String toString() {
        return parameterIdentifier.toString()
                + " = "
                + (getSelectedValue() != null ? getSelectedValue().toString() : "null");
    }
}
