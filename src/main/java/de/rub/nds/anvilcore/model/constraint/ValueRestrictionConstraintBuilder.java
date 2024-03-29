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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class ValueRestrictionConstraintBuilder<T> {
    private final String name;
    private final DerivationScope derivationScope;

    public static <T> ValueRestrictionConstraintBuilder<T> init(
            String name, DerivationScope derivationScope) {
        return new ValueRestrictionConstraintBuilder<>(name, derivationScope);
    }

    public static <T> ValueRestrictionConstraintBuilder<T> init(DerivationScope derivationScope) {
        return new ValueRestrictionConstraintBuilder<>(null, derivationScope);
    }

    private ValueRestrictionConstraintBuilder(String name, DerivationScope derivationScope) {
        this.derivationScope = derivationScope;
        this.name = name;
    }

    public ValueRestrictionConstraintBuilder1<T> target(DerivationParameter target) {
        return new ValueRestrictionConstraintBuilder1<T>(name, derivationScope, target);
    }

    public static final class ValueRestrictionConstraintBuilder1<T> {
        private final String name;
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters = new HashSet<>();

        private ValueRestrictionConstraintBuilder1(
                String name, DerivationScope derivationScope, DerivationParameter target) {
            this.name = name;
            this.derivationScope = derivationScope;
            this.target = target;
        }

        public ValueRestrictionConstraintBuilder1<T> requiredParameter(
                ParameterIdentifier requiredParameter) {
            requiredParameters.add(requiredParameter);
            return this;
        }

        public ValueRestrictionConstraintBuilder1<T> requiredParameters(
                Collection<ParameterIdentifier> requiredParameter) {
            requiredParameters.addAll(requiredParameter);
            return this;
        }

        public ValueRestrictionConstraintBuilder2<T> restrictValues(List<T> restrictedValues) {
            return new ValueRestrictionConstraintBuilder2<T>(
                    name,
                    derivationScope,
                    target,
                    requiredParameters,
                    restrictedValues,
                    null,
                    null,
                    null);
        }

        public ValueRestrictionConstraintBuilder2<T> allowValues(List<T> allowedValues) {
            return new ValueRestrictionConstraintBuilder2<T>(
                    name,
                    derivationScope,
                    target,
                    requiredParameters,
                    null,
                    allowedValues,
                    null,
                    null);
        }

        public ValueRestrictionConstraintBuilder2<T> restrictValues(
                BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                        restrictedValuesSupplier) {
            return new ValueRestrictionConstraintBuilder2<T>(
                    name,
                    derivationScope,
                    target,
                    requiredParameters,
                    null,
                    null,
                    restrictedValuesSupplier,
                    null);
        }

        public ValueRestrictionConstraintBuilder2<T> allowValues(
                BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                        restrictedValuesSupplier) {
            return new ValueRestrictionConstraintBuilder2<T>(
                    name,
                    derivationScope,
                    target,
                    requiredParameters,
                    null,
                    null,
                    null,
                    restrictedValuesSupplier);
        }
    }

    public static final class ValueRestrictionConstraintBuilder2<T> {
        private final String name;
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters;
        private final List<T> restrictedValues;
        private final List<T> allowedValues;
        private final BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                restrictedValuesSupplier;
        private final BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                allowedValuesSupplier;

        private ValueRestrictionConstraintBuilder2(
                String name,
                DerivationScope derivationScope,
                DerivationParameter target,
                Set<ParameterIdentifier> requiredParameters,
                List<T> restrictedValues,
                List<T> allowedValues,
                BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                        restrictedValuesSupplier,
                BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                        allowedValuesSupplier) {
            this.name = name;
            this.derivationScope = derivationScope;
            this.target = target;
            this.requiredParameters = requiredParameters;
            this.restrictedValues = restrictedValues;
            this.allowedValues = allowedValues;
            this.restrictedValuesSupplier = restrictedValuesSupplier;
            this.allowedValuesSupplier = allowedValuesSupplier;
        }

        public ValueRestrictionConstraintBuilder3<T> condition(
                BiPredicate<DerivationParameter, List<DerivationParameter>> condition) {
            return new ValueRestrictionConstraintBuilder3<>(
                    name,
                    derivationScope,
                    target,
                    requiredParameters,
                    restrictedValues,
                    allowedValues,
                    restrictedValuesSupplier,
                    allowedValuesSupplier,
                    condition);
        }
    }

    public static final class ValueRestrictionConstraintBuilder3<T> {
        private final String name;
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters;
        private List<T> restrictedValues;
        private List<T> allowedValues;
        private final BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                restrictedValuesSupplier;
        private final BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                allowedValuesSupplier;
        private final BiPredicate<DerivationParameter, List<DerivationParameter>> condition;

        private ValueRestrictionConstraintBuilder3(
                String name,
                DerivationScope derivationScope,
                DerivationParameter target,
                Set<ParameterIdentifier> requiredParameters,
                List<T> restrictedValues,
                List<T> allowedValues,
                BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                        restrictedValuesSupplier,
                BiFunction<DerivationParameter, List<DerivationParameter>, List<T>>
                        allowedValuesSupplier,
                BiPredicate<DerivationParameter, List<DerivationParameter>> condition) {
            this.name = name;
            this.derivationScope = derivationScope;
            this.target = target;
            this.requiredParameters = requiredParameters;
            this.restrictedValues = restrictedValues;
            this.allowedValues = allowedValues;
            this.restrictedValuesSupplier = restrictedValuesSupplier;
            this.allowedValuesSupplier = allowedValuesSupplier;
            this.condition = condition;
        }

        public FlexibleConditionalConstraint get() {
            BiPredicate<DerivationParameter, List<DerivationParameter>> predicate =
                    (target, requiredParameters) -> {
                        if (restrictedValuesSupplier != null) {
                            restrictedValues =
                                    restrictedValuesSupplier.apply(target, requiredParameters);
                        }
                        if (allowedValuesSupplier != null) {
                            allowedValues = allowedValuesSupplier.apply(target, requiredParameters);
                        }
                        if (restrictedValues != null) {
                            if (condition.test(target, requiredParameters)) {
                                return !restrictedValues.contains(target.getSelectedValue());
                            }
                        }
                        if (allowedValues != null) {
                            if (condition.test(target, requiredParameters)) {
                                return allowedValues.contains(target.getSelectedValue());
                            }
                        }
                        return true;
                    };

            return new FlexibleConditionalConstraint(
                    name, derivationScope, target, requiredParameters, predicate);
        }
    }
}
