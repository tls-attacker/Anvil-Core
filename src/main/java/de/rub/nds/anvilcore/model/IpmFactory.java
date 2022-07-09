package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.InputParameterModel;
import de.rwth.swc.coffee4j.model.Parameter;
import de.rwth.swc.coffee4j.model.constraints.Constraint;

import java.util.ArrayList;
import java.util.List;

public class IpmFactory {
    public static InputParameterModel generateIpm(DerivationScope derivationScope) {
        List<ParameterIdentifier> parameterIdentifiers = getParameterIdentifiersForScope(derivationScope);
        Parameter.Builder[] builders = getParameterBuilder(parameterIdentifiers, derivationScope);
        Constraint[] constraints = getConstraintsForScope(parameterIdentifiers, derivationScope);

        return InputParameterModel.inputParameterModel("dynamic-model")
                .strength(AnvilContext.getInstance().getTestStrength())
                .parameters(builders)
                .exclusionConstraints(constraints)
                .build();
    }

    private static List<ParameterIdentifier> getParameterIdentifiersForScope(DerivationScope derivationScope) {
        // Get base parameters of model
        List<ParameterIdentifier> parameterIdentifiers = AnvilContext.getInstance().getModelBasedIpmFactory().getModelParameterIdentifiers(derivationScope);
        // Add explicit extensions
        parameterIdentifiers.addAll(derivationScope.getIpmExtensions());
        // Remove explicit limitations
        parameterIdentifiers.removeAll(derivationScope.getIpmLimitations());

        return parameterIdentifiers;
    }

    private static Parameter.Builder[] getParameterBuilder(List<ParameterIdentifier> parameterIdentifiers, DerivationScope derivationScope) {
        List<Parameter.Builder> parameterBuilders = new ArrayList<>();
        for (ParameterIdentifier parameterIdentifier : parameterIdentifiers) {
            DerivationParameter<?,?> parameter = ParameterFactory.getInstanceFromIdentifier(parameterIdentifier);
            if (parameter.canBeModeled(derivationScope)) {
                parameterBuilders.add(parameter.getParameterBuilder(derivationScope));
                // TODO: How to model bitmask params from tls anvil?
            }
        }
        return parameterBuilders.toArray(new Parameter.Builder[]{});
    }

    private static Constraint[] getConstraintsForScope(List<ParameterIdentifier> parameterIdentifiers, DerivationScope derivationScope) {
        List<Constraint> applicableConstraints = new ArrayList<>();
        for (ParameterIdentifier parameterIdentifier : parameterIdentifiers) {
            DerivationParameter<?,?> parameter = ParameterFactory.getInstanceFromIdentifier(parameterIdentifier);
            if (parameter.canBeModeled(derivationScope)) {
                List<ConditionalConstraint> conditionalConstraints = parameter.getConditionalConstraints(derivationScope);
                for (ConditionalConstraint conditionalConstraint : conditionalConstraints) {
                    if (conditionalConstraint.isApplicableTo(parameterIdentifiers, derivationScope)) {
                        applicableConstraints.add(conditionalConstraint.getConstraint());
                    }
                }
            }
        }
        return applicableConstraints.toArray(new Constraint[]{});
    }
}