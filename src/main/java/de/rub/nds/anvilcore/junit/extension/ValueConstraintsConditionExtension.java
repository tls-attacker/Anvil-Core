package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ValueConstraintsConditionExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if (extensionContext.getTestMethod().isEmpty()) {
            return ConditionEvaluationResult.enabled("Class annotations are not relevant");
        }

        DerivationScope derivationScope = new DerivationScope(extensionContext);
        for (ValueConstraint valueConstraint : derivationScope.getValueConstraints()) {
            DerivationParameter derivationParameter = ParameterFactory.getInstanceFromIdentifier(valueConstraint.getAffectedParameter());
            if (derivationParameter.hasNoApplicableValues(derivationScope)) {
                return ConditionEvaluationResult.disabled("No values supported required for parameter "
                        + derivationParameter.getParameterIdentifier());
            }
        }
        for (ParameterIdentifier explicitParameterIdentifier : derivationScope.getExplicitValues().keySet()) {
            DerivationParameter derivationParameter = ParameterFactory.getInstanceFromIdentifier(explicitParameterIdentifier);
            if (derivationParameter.hasNoApplicableValues(derivationScope)) {
                return ConditionEvaluationResult.disabled("No values supported required for parameter " + explicitParameterIdentifier);
            }
        }
        return ConditionEvaluationResult.enabled("");
    }
}
