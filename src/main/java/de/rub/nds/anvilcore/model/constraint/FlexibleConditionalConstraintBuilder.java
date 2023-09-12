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
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class FlexibleConditionalConstraintBuilder {
    private final String name;
    private final DerivationScope derivationScope;

    public static FlexibleConditionalConstraintBuilder init(
            String name, DerivationScope derivationScope) {
        return new FlexibleConditionalConstraintBuilder(name, derivationScope);
    }

    public static FlexibleConditionalConstraintBuilder init(DerivationScope derivationScope) {
        return new FlexibleConditionalConstraintBuilder(
                "flexible-conditional-constraint", derivationScope);
    }

    private FlexibleConditionalConstraintBuilder(String name, DerivationScope derivationScope) {
        this.derivationScope = derivationScope;
        this.name = name;
    }

    public FlexibleConditionalConstraintBuilder1 target(DerivationParameter target) {
        return new FlexibleConditionalConstraintBuilder1(name, derivationScope, target);
    }

    public static final class FlexibleConditionalConstraintBuilder1 {
        private final String name;
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters = new HashSet<>();

        private FlexibleConditionalConstraintBuilder1(
                String name, DerivationScope derivationScope, DerivationParameter target) {
            this.name = name;
            this.derivationScope = derivationScope;
            this.target = target;
        }

        public FlexibleConditionalConstraintBuilder1 requiredParameter(
                ParameterIdentifier requiredParameter) {
            requiredParameters.add(requiredParameter);
            return this;
        }

        public FlexibleConditionalConstraintBuilder1 requiredParameters(
                Collection<ParameterIdentifier> requiredParameter) {
            requiredParameters.addAll(requiredParameter);
            return this;
        }

        public FlexibleConditionalConstraintBuilder2 predicate(
                BiPredicate<DerivationParameter, List<DerivationParameter>> predicate) {
            return new FlexibleConditionalConstraintBuilder2(
                    name, derivationScope, target, requiredParameters, predicate);
        }
    }

    public static final class FlexibleConditionalConstraintBuilder2 {
        private final String name;
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters;
        private final BiPredicate<DerivationParameter, List<DerivationParameter>> predicate;

        private FlexibleConditionalConstraintBuilder2(
                String name,
                DerivationScope derivationScope,
                DerivationParameter target,
                Set<ParameterIdentifier> requiredParameters,
                BiPredicate<DerivationParameter, List<DerivationParameter>> predicate) {
            this.name = name;
            this.derivationScope = derivationScope;
            this.target = target;
            this.requiredParameters = requiredParameters;
            this.predicate = predicate;
        }

        public FlexibleConditionalConstraint get() {
            return new FlexibleConditionalConstraint(
                    name, derivationScope, target, requiredParameters, predicate);
        }
    }
}
