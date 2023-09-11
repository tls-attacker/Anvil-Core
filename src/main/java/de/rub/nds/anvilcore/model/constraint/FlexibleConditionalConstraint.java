package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.constraints.Constraint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

@SuppressWarnings("rawtypes")
public class FlexibleConditionalConstraint extends ConditionalConstraint {
    private final String constraintName;
    private final DerivationScope derivationScope;
    private final DerivationParameter target;
    private final Set<ParameterIdentifier> requiredParameters;
    private final Set<ParameterIdentifier> dynamicParameters = new HashSet<>();
    private final Set<ParameterIdentifier> staticParameters = new HashSet<>();
    private final BiPredicate<DerivationParameter, List<DerivationParameter>> predicate;
    private final boolean staticTarget;

    public FlexibleConditionalConstraint(
            String constraintName,
            DerivationScope derivationScope,
            DerivationParameter target,
            Set<ParameterIdentifier> requiredParameters,
            BiPredicate<DerivationParameter, List<DerivationParameter>> predicate) {
        this.constraintName = constraintName;
        this.derivationScope = derivationScope;
        this.target = target;
        this.requiredParameters = requiredParameters;
        this.predicate = predicate;

        // Partition required parameters into static and dynamic parameters
        for (ParameterIdentifier parameterIdentifier : requiredParameters) {
            DerivationParameter parameter = parameterIdentifier.getInstance();
            if (parameter.canBeModeled(derivationScope)) {
                dynamicParameters.add(parameterIdentifier);
            } else {
                staticParameters.add(parameterIdentifier);
            }
        }

        staticTarget = !target.canBeModeled(derivationScope);

        setRequiredParameters(dynamicParameters);
        List<String> dynamicParameterNames = new ArrayList<>();
        if (!staticTarget) {
            dynamicParameterNames.add(target.getParameterIdentifier().name());
        }
        for (ParameterIdentifier parameterIdentifier : dynamicParameters) {
            dynamicParameterNames.add(parameterIdentifier.name());
        }
        Constraint constraint =
                new Constraint(constraintName, dynamicParameterNames, this::predicateAdapter);
        setConstraint(constraint);
    }

    private boolean predicateAdapter(List<?> objects) {
        // Cast objects
        List<DerivationParameter> parameterValues = new ArrayList<>();
        for (Object obj : objects) {
            if (obj instanceof DerivationParameter) {
                parameterValues.add((DerivationParameter) obj);
            } else {
                throw new IllegalArgumentException(
                        "Object passed to constraint is not a DerivationParameter");
            }
        }

        // Check dynamic parameter values
        for (int i = 1; i < parameterValues.size(); i++) {
            if (!dynamicParameters.contains(parameterValues.get(i).getParameterIdentifier())) {
                throw new IllegalStateException(
                        "Unexpected dynamic parameter: "
                                + parameterValues.get(i).getParameterIdentifier());
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
            // Check dynamic parameter values
            if (parameterValues.size() == 0
                    || !parameterValues
                            .get(0)
                            .getParameterIdentifier()
                            .equals(target.getParameterIdentifier())) {
                throw new IllegalArgumentException(
                        "The first parameter passed to constraint does not match target parameter");
            }

            // Remove target values from parameter value list
            targetValue = parameterValues.get(0);
            parameterValues.remove(0);
        }

        // Add static parameter values manually
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
            parameterValues.add(staticValue.get(0));
        }

        // Call constraint predicate
        return predicate.test(targetValue, parameterValues);
    }
}
