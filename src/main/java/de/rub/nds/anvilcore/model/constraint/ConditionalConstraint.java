/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.constraints.Constraint;
import java.util.List;
import java.util.Set;

public class ConditionalConstraint {
    private Set<ParameterIdentifier> requiredParameters;
    private Constraint constraint;

    public ConditionalConstraint(
            Set<ParameterIdentifier> requiredParameters, Constraint constraint) {
        this.requiredParameters = requiredParameters;
        this.constraint = constraint;
    }

    public Set<ParameterIdentifier> getRequiredParameters() {
        return requiredParameters;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    protected ConditionalConstraint() {}

    protected void setRequiredParameters(Set<ParameterIdentifier> requiredParameters) {
        this.requiredParameters = requiredParameters;
    }

    protected void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    public boolean isApplicableTo(
            List<ParameterIdentifier> modeledParameters, DerivationScope derivationScope) {
        for (ParameterIdentifier required : requiredParameters) {
            if (!modeledParameters.contains(required)
                    || !required.getInstance().canBeModeled(derivationScope)) {
                return false;
            }
        }
        return true;
    }
}
