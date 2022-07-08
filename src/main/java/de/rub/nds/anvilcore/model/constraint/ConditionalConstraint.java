package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.context.TestContext;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.constraints.Constraint;

import java.util.List;
import java.util.Set;

public class ConditionalConstraint {
    private final Set<ParameterIdentifier> requiredParameters;
    private final Constraint constraint;

    public Set<ParameterIdentifier> getRequiredParameters() {
        return requiredParameters;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public boolean isApplicableTo(List<ParameterIdentifier> modeledParameters, DerivationScope derivationScope) {
        for (ParameterIdentifier required: requiredParameters) {
            if (!modeledParameters.contains(required) || !ParameterFactory.getInstanceFromIdentifier(required).canBeModeled(derivationScope)) {
                return false;
            }
        }
        return true;
    }

    public ConditionalConstraint(Set<ParameterIdentifier> requiredParameters, Constraint constraint) {
        this.requiredParameters = requiredParameters;
        this.constraint = constraint;
    }
}
