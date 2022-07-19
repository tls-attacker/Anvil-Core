package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class AggregatedEnableConstraintBuilder {
    private final DerivationScope derivationScope;

    public static AggregatedEnableConstraintBuilder init(DerivationScope derivationScope) {
        return new AggregatedEnableConstraintBuilder(derivationScope);
    }

    private AggregatedEnableConstraintBuilder(DerivationScope derivationScope) {
        this.derivationScope = derivationScope;
    }

    public AggregatedEnableConstraintBuilder1 constrain(DerivationParameter target) {
        return new AggregatedEnableConstraintBuilder1(derivationScope, target);
    }

    @SuppressWarnings("rawtypes")
    public static final class AggregatedEnableConstraintBuilder1 {
        private final DerivationScope derivationScope;
        private final DerivationParameter target;
        private final Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions = new HashMap<>();

        public AggregatedEnableConstraintBuilder1(DerivationScope derivationScope, DerivationParameter target) {
            this.derivationScope = derivationScope;
            this.target = target;
        }

        public AggregatedEnableConstraintBuilder1 condition(ParameterIdentifier subject,
                                                            Predicate<DerivationParameter> condition) {
            conditions.put(subject, condition);
            return this;
        }

        public AggregatedEnableConstraintBuilder1 conditions(Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions) {
            this.conditions.putAll(conditions);
            return this;
        }

        public AggregatedEnableConstraint get() {
            return new AggregatedEnableConstraint(derivationScope, target, conditions);
        }
    }
}
