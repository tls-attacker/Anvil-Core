package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
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

        AnvilTestTemplate anvilTestTemplate = new AnvilTestTemplate(extensionContext);
        for (ValueConstraint valueConstraint : anvilTestTemplate.getValueConstraints()) {
            DerivationParameter derivationParameter =
                    valueConstraint.getAffectedParameter().getInstance();
            if (derivationParameter.hasNoApplicableValues(anvilTestTemplate)) {
                return ConditionEvaluationResult.disabled(
                        "No values supported required for parameter "
                                + derivationParameter.getParameterIdentifier());
            }
        }
        for (ParameterIdentifier explicitParameterIdentifier :
                anvilTestTemplate.getExplicitValues().keySet()) {
            DerivationParameter derivationParameter = explicitParameterIdentifier.getInstance();
            if (derivationParameter.hasNoApplicableValues(anvilTestTemplate)) {
                return ConditionEvaluationResult.disabled(
                        "No values supported required for parameter "
                                + explicitParameterIdentifier);
            }
        }
        return ConditionEvaluationResult.enabled("");
    }
}
