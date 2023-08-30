package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
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

    public void preProcessConfig(ConfigType config, AnvilTestTemplate anvilTestTemplate) {}
    ;

    public abstract void applyToConfig(ConfigType config, AnvilTestTemplate anvilTestTemplate);

    public void postProcessConfig(ConfigType config, AnvilTestTemplate anvilTestTemplate) {}
    ;

    public abstract List<DerivationParameter<ConfigType, ValueType>> getParameterValues(
            AnvilTestTemplate anvilTestTemplate);

    public List<DerivationParameter<ConfigType, ValueType>> getConstrainedParameterValues(
            AnvilTestTemplate anvilTestTemplate) {
        if (anvilTestTemplate.hasExplicitValues(parameterIdentifier)) {
            return getExplicitValues(anvilTestTemplate);
        } else {
            return getParameterValues(anvilTestTemplate).stream()
                    .filter(
                            value ->
                                    valueApplicableUnderAllConstraints(
                                            anvilTestTemplate.getValueConstraints(),
                                            (ValueType) value.getSelectedValue()))
                    .collect(Collectors.toList());
        }
    }

    public List<ConditionalConstraint> getDefaultConditionalConstraints(
            AnvilTestTemplate anvilTestTemplate) {
        return new ArrayList<>();
    }

    public List<ConditionalConstraint> getConditionalConstraints(
            AnvilTestTemplate anvilTestTemplate) {
        if (anvilTestTemplate.hasExplicitModelingConstraints(parameterIdentifier)) {
            return getExplicitModelingConstraints(anvilTestTemplate);
        } else {
            // return Collections.emptyList();
            return getDefaultConditionalConstraints(anvilTestTemplate);
        }
    }

    public Parameter.Builder getParameterBuilder(AnvilTestTemplate anvilTestTemplate) {
        List<DerivationParameter<ConfigType, ValueType>> parameterValues =
                getConstrainedParameterValues(anvilTestTemplate);
        return Parameter.parameter(parameterIdentifier.toString())
                .values(parameterValues.toArray());
    }

    public boolean canBeModeled(AnvilTestTemplate anvilTestTemplate) {
        return getConstrainedParameterValues(anvilTestTemplate).size() > 1;
    }

    public boolean hasNoApplicableValues(AnvilTestTemplate anvilTestTemplate) {
        return getConstrainedParameterValues(anvilTestTemplate).isEmpty();
    }

    protected abstract DerivationParameter<ConfigType, ValueType> generateValue(
            ValueType selectedValue);

    private List<DerivationParameter<ConfigType, ValueType>> getExplicitValues(
            AnvilTestTemplate anvilTestTemplate) {
        try {
            String methodName = anvilTestTemplate.getExplicitValues().get(parameterIdentifier);
            Method method =
                    anvilTestTemplate
                            .getExtensionContext()
                            .getRequiredTestClass()
                            .getMethod(methodName, AnvilTestTemplate.class);
            Constructor constructor =
                    anvilTestTemplate.getExtensionContext().getRequiredTestClass().getConstructor();
            return (List<DerivationParameter<ConfigType, ValueType>>)
                    method.invoke(constructor.newInstance(), anvilTestTemplate);
        } catch (NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException
                | InstantiationException e) {
            LOGGER.error("Was unable to fetch explicit values for type " + parameterIdentifier, e);
            return Collections.emptyList();
        }
    }

    private boolean valueApplicableUnderAllConstraints(
            List<ValueConstraint> valueConstraints, ValueType value) {
        for (ValueConstraint constraint : valueConstraints) {
            if (constraint.getAffectedParameter().equals(parameterIdentifier)) {
                if (!valueApplicableUnderConstraint(constraint, value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean valueApplicableUnderConstraint(ValueConstraint constraint, ValueType value) {
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
                return (Boolean) method.invoke(constructor.newInstance(), value);
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
            AnvilTestTemplate anvilTestTemplate) {
        try {
            String methodName =
                    anvilTestTemplate.getExplicitModelingConstraints().get(parameterIdentifier);
            Method method =
                    anvilTestTemplate
                            .getExtensionContext()
                            .getRequiredTestClass()
                            .getMethod(methodName, AnvilTestTemplate.class);
            Constructor constructor =
                    anvilTestTemplate.getExtensionContext().getRequiredTestClass().getConstructor();

            return (List<ConditionalConstraint>)
                    method.invoke(constructor.newInstance(), anvilTestTemplate);
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

    @Override
    public String toString() {
        return parameterIdentifier.toString()
                + " = "
                + (getSelectedValue() != null ? getSelectedValue().toString() : "null");
    }
}
