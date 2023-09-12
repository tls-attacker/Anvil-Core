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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class AggregatedEnableConstraintBuilder<T> {
    private final DerivationScope derivationScope;

    public static <T> AggregatedEnableConstraintBuilder<T> init(DerivationScope derivationScope) {
        return new AggregatedEnableConstraintBuilder<>(derivationScope);
    }

    private AggregatedEnableConstraintBuilder(DerivationScope derivationScope) {
        this.derivationScope = derivationScope;
    }

    public AggregatedEnableConstraintBuilder1<T> constrain(DerivationParameter target) {
        return new AggregatedEnableConstraintBuilder1<>(derivationScope, target);
    }

    @SuppressWarnings("rawtypes")
    public static final class AggregatedEnableConstraintBuilder1<T> {
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions =
                new HashMap<>();
        private T defaultValue = null;

        private AggregatedEnableConstraintBuilder1(
                DerivationScope derivationScope, DerivationParameter target) {
            this.derivationScope = derivationScope;
            this.target = target;
        }

        public AggregatedEnableConstraintBuilder1<T> condition(
                ParameterIdentifier subject, Predicate<DerivationParameter> condition) {
            conditions.put(subject, condition);
            return this;
        }

        public AggregatedEnableConstraintBuilder1<T> conditions(
                Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions) {
            this.conditions.putAll(conditions);
            return this;
        }

        public AggregatedEnableConstraintBuilder1<T> defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public AggregatedEnableConstraint<T> get() {
            return new AggregatedEnableConstraint<T>(
                    derivationScope, target, defaultValue, conditions);
        }
    }
}
