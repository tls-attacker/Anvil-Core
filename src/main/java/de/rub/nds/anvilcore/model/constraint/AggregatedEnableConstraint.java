package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.constraints.Constraint;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class AggregatedEnableConstraint<T> extends ConditionalConstraint {
    private final DerivationScope derivationScope;
    private final DerivationParameter target;
    private final T defaultValue;
    private final Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions;
    private final Set<ParameterIdentifier> dynamicParameters = new HashSet<>();
    private final Set<ParameterIdentifier> staticParameters = new HashSet<>();
    private final boolean staticTarget;

    public AggregatedEnableConstraint(
            DerivationScope derivationScope,
            DerivationParameter target,
            T defaultValue,
            Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions) {
        this.derivationScope = derivationScope;
        this.target = target;
        this.defaultValue = defaultValue;
        this.conditions = conditions;

        // Partition required parameters into static and dynamic parameter
        for (ParameterIdentifier parameterIdentifier : conditions.keySet()) {
            DerivationParameter parameter = parameterIdentifier.getInstance();
            if (parameter.canBeModeled(derivationScope)) {
                dynamicParameters.add(parameterIdentifier);
            } else {
                staticParameters.add(parameterIdentifier);
            }
        }
        setRequiredParameters(dynamicParameters);

        staticTarget = !target.canBeModeled(derivationScope);

        List<String> parameterNames = new ArrayList<>();
        if (!staticTarget) {
            parameterNames.add(target.getParameterIdentifier().name());
        }
        for (ParameterIdentifier dynamicParameterIdentifier : dynamicParameters) {
            parameterNames.add(dynamicParameterIdentifier.name());
        }
        Constraint constraint =
                new Constraint(
                        "aggregated-enable-constraint",
                        parameterNames,
                        this::aggregatedPredicateAdapter);
        setConstraint(constraint);
    }

    private boolean aggregatedPredicateAdapter(List<?> objects) {
        List<DerivationParameter> derivationParameters = new ArrayList<>();
        for (Object obj : objects) {
            if (obj instanceof DerivationParameter) {
                derivationParameters.add((DerivationParameter) obj);
            } else {
                throw new IllegalArgumentException(
                        "Object passed to constraint is not a DerivationParameter");
            }
        }
        return aggregatedPredicate(derivationParameters);
    }

    private boolean aggregatedPredicate(List<DerivationParameter> dynamicParameterValues) {
        // Check dynamic parameter values
        for (int i = 1; i < dynamicParameterValues.size(); i++) {
            if (!dynamicParameters.contains(
                    dynamicParameterValues.get(i).getParameterIdentifier())) {
                throw new IllegalStateException(
                        "Unexpected dynamic parameter: "
                                + dynamicParameterValues.get(i).getParameterIdentifier());
            }
        }

        DerivationParameter targetValue;
        if (staticTarget) {
            // Get static target value
            targetValue = target.getParameterIdentifier().getInstance();
            List<DerivationParameter> values =
                    targetValue.getConstrainedParameterValues(derivationScope);
            if (values.size() != 1) {
                throw new IllegalStateException(
                        "Static target parameter does not have exactly 1 value");
            }
            targetValue = values.get(0);
        } else {
            if (dynamicParameterValues.size() == 0
                    || !dynamicParameterValues
                            .get(0)
                            .getParameterIdentifier()
                            .equals(target.getParameterIdentifier())) {
                throw new IllegalArgumentException(
                        "The first parameter passed to constraint does not match target parameter");
            }
            // Remove target value from parameter value list
            targetValue = dynamicParameterValues.get(0);
            dynamicParameterValues.remove(0);
        }

        // Add static parameter values
        List<DerivationParameter> allParameterValues = dynamicParameterValues;
        for (ParameterIdentifier staticParameterIdentifier : staticParameters) {
            DerivationParameter staticParameter = staticParameterIdentifier.getInstance();
            List<DerivationParameter> staticValue =
                    staticParameter.getConstrainedParameterValues(derivationScope);
            if (staticValue.size() != 1) {
                throw new IllegalStateException(
                        "Static parameter "
                                + staticParameterIdentifier
                                + " does not have exactly 1 value");
            }
            allParameterValues.add(staticValue.get(0));
        }

        // The result of the aggregated constraint is true if all sub constraints return true
        boolean enabled = true;
        for (DerivationParameter parameterValue : allParameterValues) {
            Predicate<DerivationParameter> subCondition =
                    conditions.get(parameterValue.getParameterIdentifier());
            if (!subCondition.test(parameterValue)) {
                enabled = false;
                break;
            }
        }

        // If no defaultValue is specified, the parameter is set to null IF AND ONLY IF it is
        // disabled
        if (defaultValue == null) {
            return enabled ^ targetValue.getSelectedValue() == null;
        }
        // If defaultValue is specified, the parameter is set to that value IF it is disabled
        else {
            return enabled || Objects.equals(targetValue.getSelectedValue(), defaultValue);
        }
    }
}
