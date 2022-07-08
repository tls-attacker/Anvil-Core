package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rwth.swc.coffee4j.model.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DerivationParameter<ConfigType, ValueType> {
    private static final Logger LOGGER = LogManager.getLogger();

    private ValueType selectedValue;
    private final Class<ValueType> valueClass;
    private final ParameterIdentifier parameterIdentifier;

    public DerivationParameter(Class<ValueType> valueClass, ParameterIdentifier parameterIdentifier) {
        this.valueClass = valueClass;
        this.parameterIdentifier = parameterIdentifier;
    }

    public ValueType getSelectedValue() {
        return selectedValue;
    }

    public Class<ValueType> getValueClass() {
        return valueClass;
    }

    public ParameterIdentifier getParameterIdentifier() {
        return parameterIdentifier;
    }

    public void preProcessConfig(ConfigType config) {};

    public abstract void applyToConfig(ConfigType config);

    public void postProcessConfig(ConfigType config) {};

    public abstract List<DerivationParameter<ConfigType, ValueType>> getParameterValues(DerivationScope derivationScope);

    public List<DerivationParameter<ConfigType, ValueType>> getConstrainedParameterValues(DerivationScope derivationScope) {
        if (derivationScope.hasExplicitValues(parameterIdentifier)) {
            return getExplicitValues(derivationScope);
        } else {
            return getParameterValues(derivationScope).stream()
                    .filter(value -> valueApplicableUnderAllConstraints(derivationScope.getValueConstraints(), value.getSelectedValue()))
                    .collect(Collectors.toList());
        }
    }

    public List<ConditionalConstraint> getDefaultConditionalConstraints(DerivationScope derivationScope) {
        return new ArrayList<>();
    }

    public List<ConditionalConstraint> getConditionalConstraints(DerivationScope derivationScope) {
        if(derivationScope.hasExplicitModelingConstraints(parameterIdentifier)) {
            return getExplicitModelingConstraints(derivationScope);
        } else {
            return getDefaultConditionalConstraints(derivationScope);
        }
    }

    public Parameter.Builder getParameterBuilder(DerivationScope derivationScope) {
        List<DerivationParameter<ConfigType, ValueType>> parameterValues = getConstrainedParameterValues(derivationScope);
        return Parameter
                .parameter(parameterIdentifier.toString())
                .values(parameterValues.toArray());
    }

    public boolean canBeModeled(DerivationScope derivationScope) {
        return getConstrainedParameterValues(derivationScope).size() > 1;
    }



    protected abstract DerivationParameter<ConfigType, ValueType> generateValue(ValueType selectedValue);

    private List<DerivationParameter<ConfigType, ValueType>> getExplicitValues(DerivationScope derivationScope) {
        try {
            String methodName = derivationScope.getExplicitValues().get(parameterIdentifier);
            Method method = derivationScope.getExtensionContext().getRequiredTestClass().getMethod(methodName, DerivationScope.class);
            Constructor constructor = derivationScope.getExtensionContext().getRequiredTestClass().getConstructor();
            return (List<DerivationParameter<ConfigType, ValueType>>) method.invoke(constructor.newInstance(), derivationScope);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("Was unable to fetch explicit values for type " + parameterIdentifier, e);
            return Collections.emptyList();
        }
    }

    private boolean valueApplicableUnderAllConstraints(List<ValueConstraint> valueConstraints, ValueType value) {
        for (ValueConstraint constraint : valueConstraints) {
            if (constraint.getAffectedParameter() == parameterIdentifier) {
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
                method = constraint.getClazz().getMethod(constraint.getEvaluationMethod(), valueClass);
                constructor = constraint.getClazz().getConstructor();
                return (Boolean) method.invoke(constructor.newInstance(), value);
            } else {
                method = valueClass.getMethod(constraint.getEvaluationMethod());
                return (Boolean) method.invoke(constraint);
            }
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            LOGGER.error("Was unable to invoke constraint method for type " + parameterIdentifier, e);
            return true;
        }
    }

    private List<ConditionalConstraint> getExplicitModelingConstraints(DerivationScope derivationScope) {
        try {
            String methodName = derivationScope.getExplicitModelingConstraints().get(parameterIdentifier);
            Method method = derivationScope.getExtensionContext().getRequiredTestClass().getMethod(methodName, DerivationScope.class);
            Constructor constructor = derivationScope.getExtensionContext().getRequiredTestClass().getConstructor();

            return (List<ConditionalConstraint>) method.invoke(constructor.newInstance(), derivationScope);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("Was unable to fetch explicit constraints for type " + parameterIdentifier, e);
            return new ArrayList<>();
        }
    }
}
