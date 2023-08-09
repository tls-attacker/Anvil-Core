package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class AggregatedEnableConstraintBuilder<T> {
    private final AnvilTestTemplate anvilTestTemplate;

    public static <T> AggregatedEnableConstraintBuilder<T> init(
            AnvilTestTemplate anvilTestTemplate) {
        return new AggregatedEnableConstraintBuilder<>(anvilTestTemplate);
    }

    private AggregatedEnableConstraintBuilder(AnvilTestTemplate anvilTestTemplate) {
        this.anvilTestTemplate = anvilTestTemplate;
    }

    public AggregatedEnableConstraintBuilder1<T> constrain(DerivationParameter target) {
        return new AggregatedEnableConstraintBuilder1<>(anvilTestTemplate, target);
    }

    @SuppressWarnings("rawtypes")
    public static final class AggregatedEnableConstraintBuilder1<T> {
        private final AnvilTestTemplate anvilTestTemplate;
        private final DerivationParameter target;
        private final Map<ParameterIdentifier, Predicate<DerivationParameter>> conditions =
                new HashMap<>();
        private T defaultValue = null;

        private AggregatedEnableConstraintBuilder1(
                AnvilTestTemplate anvilTestTemplate, DerivationParameter target) {
            this.anvilTestTemplate = anvilTestTemplate;
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
                    anvilTestTemplate, target, defaultValue, conditions);
        }
    }
}
