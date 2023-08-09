package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
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
            List<ParameterIdentifier> modeledParameters, AnvilTestTemplate anvilTestTemplate) {
        for (ParameterIdentifier required : requiredParameters) {
            if (!modeledParameters.contains(required)
                    || !required.getInstance().canBeModeled(anvilTestTemplate)) {
                return false;
            }
        }
        return true;
    }
}
