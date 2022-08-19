package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.config.AnvilConfig;
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
import java.util.List;
import java.util.stream.Collectors;

public abstract class DerivationParameter<ConfigType extends AnvilConfig, ValueType> {
    private static final Logger LOGGER = LogManager.getLogger();

    private ValueType selectedValue;
    private final Class<ValueType> valueClass;
    private final Class<ConfigType> configClass;
    private final ParameterIdentifier parameterIdentifier;

    public DerivationParameter(Class<ValueType> valueClass, Class<ConfigType> configClass, ParameterIdentifier parameterIdentifier) {
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

    public Class<? extends AnvilConfig> getConfigClass() {
        return configClass;
    }

    public ParameterIdentifier getParameterIdentifier() {
        return parameterIdentifier;
    }

    public void preProcessConfig(ConfigType config, DerivationScope derivationScope) {};

    public abstract void applyToConfig(ConfigType config, DerivationScope derivationScope);

    public void postProcessConfig(ConfigType config, DerivationScope derivationScope) {};

    public abstract List<DerivationParameter> getParameterValues(DerivationScope derivationScope);

    public List<DerivationParameter> getConstrainedParameterValues(DerivationScope derivationScope) {
        if (derivationScope.hasExplicitValues(parameterIdentifier)) {
            return getExplicitValues(derivationScope);
        } else {
            return getParameterValues(derivationScope).stream()
                    .filter(value -> valueApplicableUnderAllConstraints(derivationScope.getValueConstraints(), (ValueType) value.getSelectedValue()))
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
            //return Collections.emptyList();
            return getDefaultConditionalConstraints(derivationScope);
        }
    }

    public Parameter.Builder getParameterBuilder(DerivationScope derivationScope) {
        List<DerivationParameter> parameterValues = getConstrainedParameterValues(derivationScope);
        return Parameter
                .parameter(parameterIdentifier.toString())
                .values(parameterValues.toArray());
    }

    public boolean canBeModeled(DerivationScope derivationScope) {
        return getConstrainedParameterValues(derivationScope).size() > 1;
    }

    public boolean hasNoApplicableValues(DerivationScope derivationScope) {
        return getConstrainedParameterValues(derivationScope).isEmpty();
    }

    protected abstract DerivationParameter<ConfigType, ValueType> generateValue(ValueType selectedValue);

    private List<DerivationParameter> getExplicitValues(DerivationScope derivationScope) {
        try {
            String methodName = derivationScope.getExplicitValues().get(parameterIdentifier);
            Method method = derivationScope.getExtensionContext().getRequiredTestClass().getMethod(methodName, DerivationScope.class);
            Constructor constructor = derivationScope.getExtensionContext().getRequiredTestClass().getConstructor();
            return (List<DerivationParameter>) method.invoke(constructor.newInstance(), derivationScope);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("Was unable to fetch explicit values for type " + parameterIdentifier, e);
            return Collections.emptyList();
        }
    }

    private boolean valueApplicableUnderAllConstraints(List<ValueConstraint> valueConstraints, ValueType value) {
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
                method = constraint.getClazz().getMethod(constraint.getEvaluationMethod(), valueClass);
                constructor = constraint.getClazz().getConstructor();
                return (Boolean) method.invoke(constructor.newInstance(), value);
            } else {
                method = constraint.getClazz().getMethod(constraint.getEvaluationMethod(), valueClass);
                return (Boolean) method.invoke(null, value);
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

    @Override
    public String toString() {
        return parameterIdentifier.toString() + " = " + (getSelectedValue() != null ? getSelectedValue().toString() : "null");
    }
}
