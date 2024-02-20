/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.coffee4j.model.ModelFromScope;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rwth.swc.coffee4j.junit.provider.model.ModelProvider;
import de.rwth.swc.coffee4j.model.InputParameterModel;
import de.rwth.swc.coffee4j.model.Parameter;
import de.rwth.swc.coffee4j.model.Value;
import de.rwth.swc.coffee4j.model.constraints.Constraint;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

public class IpmProvider implements ModelProvider, AnnotationConsumer<ModelFromScope> {
    private static final Logger LOGGER = LogManager.getLogger();

    private ModelFromScope modelFromScope;

    @Override
    public void accept(ModelFromScope modelFromScope) {
        this.modelFromScope = modelFromScope;
    }

    @Override
    public InputParameterModel provide(ExtensionContext extensionContext) {
        DerivationScope derivationScope = DerivationScope.fromExtensionContext(extensionContext);
        return generateIpm(derivationScope);
    }

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
                AnvilContext.getInstance()
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
            DerivationParameter<Object, Object> parameter = parameterIdentifier.getInstance();
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
            DerivationParameter<Object, Object> parameter = parameterIdentifier.getInstance();
            List<ConditionalConstraint> conditionalConstraints =
                    parameter.getConditionalConstraints(derivationScope);
            for (ConditionalConstraint conditionalConstraint : conditionalConstraints) {
                if (conditionalConstraint.isApplicableTo(parameterIdentifiers, derivationScope)) {
                    applicableConstraints.add(conditionalConstraint.getConstraint());
                }
            }
        }
        return applicableConstraints.toArray(Constraint[]::new);
    }
}
