/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ValueConstraintsConditionExtension extends SingleCheckCondition {

    @Override
    public ConditionEvaluationResult evaluateUncachedCondition(ExtensionContext extensionContext) {
        if (extensionContext.getTestMethod().isEmpty()) {
            return ConditionEvaluationResult.enabled("Class annotations are not relevant");
        } else {
            DerivationScope derivationScope =
                    DerivationScope.fromExtensionContext(extensionContext);
            for (ValueConstraint valueConstraint : derivationScope.getValueConstraints()) {
                DerivationParameter derivationParameter =
                        valueConstraint.getAffectedParameter().getInstance();
                if (derivationParameter.hasNoApplicableValues(derivationScope)) {
                    return ConditionEvaluationResult.disabled(
                            "No values supported required for parameter "
                                    + derivationParameter.getParameterIdentifier());
                }
            }
            for (ParameterIdentifier explicitParameterIdentifier :
                    derivationScope.getExplicitValues().keySet()) {
                DerivationParameter derivationParameter = explicitParameterIdentifier.getInstance();
                if (derivationParameter.hasNoApplicableValues(derivationScope)) {
                    return ConditionEvaluationResult.disabled(
                            "No values supported required for parameter "
                                    + explicitParameterIdentifier);
                }
            }
            return ConditionEvaluationResult.enabled("");
        }
    }
}
