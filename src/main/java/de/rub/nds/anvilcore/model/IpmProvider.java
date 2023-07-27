package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;
import de.rub.nds.anvilcore.model.config.AnvilConfig;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.model.InputParameterModel;
import de.rwth.swc.coffee4j.model.Parameter;
import de.rwth.swc.coffee4j.model.Value;
import de.rwth.swc.coffee4j.model.constraints.Constraint;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IpmProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    public static InputParameterModel generateIpm(DerivationScope derivationScope) {
        List<ParameterIdentifier> parameterIdentifiers =
                getParameterIdentifiersForScope(derivationScope);
        Parameter.Builder[] builders = getParameterBuilders(parameterIdentifiers, derivationScope);
        Constraint[] constraints = getConstraintsForScope(parameterIdentifiers, derivationScope);

        int effectiveStrength = derivationScope.getTestStrength();
        if (effectiveStrength > builders.length) {
            effectiveStrength = builders.length;
            LOGGER.info(
                    "Test {} will be executed with strength {} as the number of parameters in the IPM is lower than the configured strength {}",
                    derivationScope.getExtensionContext().getDisplayName(),
                    effectiveStrength,
                    derivationScope.getTestStrength());
        }

        return InputParameterModel.inputParameterModel("dynamic-model")
                .strength(effectiveStrength)
                .parameters(builders)
                .exclusionConstraints(constraints)
                .build();
    }

    public static List<ParameterIdentifier> getParameterIdentifiersForScope(
            DerivationScope derivationScope) {
        final List<ParameterIdentifier> parameterIdentifiers = new ArrayList<>();
        // Get base parameters of model
        parameterIdentifiers.addAll(
                AnvilFactoryRegistry.get()
                        .getParameterIdentifierProvider()
                        .getModelParameterIdentifiers(derivationScope));
        // Add explicit extensions
        parameterIdentifiers.addAll(derivationScope.getIpmExtensions());
        // Remove explicit limitations
        parameterIdentifiers.removeAll(derivationScope.getIpmLimitations());
        // Add all linked
        List<ParameterIdentifier> linkedToAdd = new LinkedList<>();
        for (ParameterIdentifier identifier : parameterIdentifiers) {
            if (identifier.hasLinkedParameterIdentifier()) {
                linkedToAdd.add(identifier.getLinkedParameterIdentifier());
            }
        }
        parameterIdentifiers.addAll(linkedToAdd);

        return parameterIdentifiers;
    }

    private static Parameter.Builder[] getParameterBuilders(
            List<ParameterIdentifier> parameterIdentifiers, DerivationScope derivationScope) {
        List<Parameter.Builder> parameterBuilders = new ArrayList<>();
        for (ParameterIdentifier parameterIdentifier : parameterIdentifiers) {
            DerivationParameter<AnvilConfig, Object> parameter =
                    ParameterFactory.getInstanceFromIdentifier(parameterIdentifier);
            if (!parameter.getConstrainedParameterValues(derivationScope).isEmpty()) {
                Parameter.Builder parameterBuilder = parameter.getParameterBuilder(derivationScope);
                parameterBuilders.add(parameterBuilder);
                if (parameterIdentifier.hasLinkedParameterIdentifier()) {
                    parameterBuilder.build().getValues().stream()
                            .map(Value::get)
                            .forEach(
                                    listedValue ->
                                            ((DerivationParameter) listedValue)
                                                    .getParameterIdentifier()
                                                    .setLinkedParameterIdentifier(
                                                            parameterIdentifier
                                                                    .getLinkedParameterIdentifier()));
                }
            }
        }
        return parameterBuilders.toArray(Parameter.Builder[]::new);
    }

    private static Constraint[] getConstraintsForScope(
            List<ParameterIdentifier> parameterIdentifiers, DerivationScope derivationScope) {
        List<Constraint> applicableConstraints = new ArrayList<>();
        for (ParameterIdentifier parameterIdentifier : parameterIdentifiers) {
            DerivationParameter<AnvilConfig, Object> parameter =
                    ParameterFactory.getInstanceFromIdentifier(parameterIdentifier);
            // if (parameter.canBeModeled(derivationScope)) {
            List<ConditionalConstraint> conditionalConstraints =
                    parameter.getConditionalConstraints(derivationScope);
            for (ConditionalConstraint conditionalConstraint : conditionalConstraints) {
                if (conditionalConstraint.isApplicableTo(parameterIdentifiers, derivationScope)) {
                    applicableConstraints.add(conditionalConstraint.getConstraint());
                }
            }
            // }
        }
        return applicableConstraints.toArray(Constraint[]::new);
    }

    /**
     * Lists the DerivationParameters which do not allow for derivation because only one value can
     * be selected. This one value will never be given to coffee4j and will be added as a
     * ParameterValue when creating the ParameterCombination from coffee4j's ArgumentsAccessor.
     *
     * @param derivationScope
     * @return List of parameters modeled with only one possible value
     */
    public static List<DerivationParameter<AnvilConfig, Object>> getStaticParameterValues(
            DerivationScope derivationScope) {
        List<DerivationParameter<AnvilConfig, Object>> staticParameterValues = new ArrayList<>();
        List<ParameterIdentifier> parameterIdentifiers =
                getParameterIdentifiersForScope(derivationScope);
        for (ParameterIdentifier parameterIdentifier : parameterIdentifiers) {
            List<DerivationParameter<AnvilConfig, Object>> parameterValues =
                    ParameterFactory.getInstanceFromIdentifier(parameterIdentifier)
                            .getConstrainedParameterValues(derivationScope);
            if (parameterValues.size() == 1) {
                staticParameterValues.add(parameterValues.get(0));
            }
        }
        return staticParameterValues;
    }
}
