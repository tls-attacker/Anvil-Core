package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class FlexibleConditionalConstraintBuilder {
    private final String name;
    private final AnvilTestTemplate anvilTestTemplate;

    public static FlexibleConditionalConstraintBuilder init(
            String name, AnvilTestTemplate anvilTestTemplate) {
        return new FlexibleConditionalConstraintBuilder(name, anvilTestTemplate);
    }

    public static FlexibleConditionalConstraintBuilder init(AnvilTestTemplate anvilTestTemplate) {
        return new FlexibleConditionalConstraintBuilder(
                "flexible-conditional-constraint", anvilTestTemplate);
    }

    private FlexibleConditionalConstraintBuilder(String name, AnvilTestTemplate anvilTestTemplate) {
        this.anvilTestTemplate = anvilTestTemplate;
        this.name = name;
    }

    public FlexibleConditionalConstraintBuilder1 target(DerivationParameter target) {
        return new FlexibleConditionalConstraintBuilder1(name, anvilTestTemplate, target);
    }

    public static final class FlexibleConditionalConstraintBuilder1 {
        private final String name;
        private final AnvilTestTemplate anvilTestTemplate;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters = new HashSet<>();

        private FlexibleConditionalConstraintBuilder1(
                String name, AnvilTestTemplate anvilTestTemplate, DerivationParameter target) {
            this.name = name;
            this.anvilTestTemplate = anvilTestTemplate;
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
                    name, anvilTestTemplate, target, requiredParameters, predicate);
        }
    }

    public static final class FlexibleConditionalConstraintBuilder2 {
        private final String name;
        private final AnvilTestTemplate anvilTestTemplate;
        private final DerivationParameter target;
        private final Set<ParameterIdentifier> requiredParameters;
        private final BiPredicate<DerivationParameter, List<DerivationParameter>> predicate;

        private FlexibleConditionalConstraintBuilder2(
                String name,
                AnvilTestTemplate anvilTestTemplate,
                DerivationParameter target,
                Set<ParameterIdentifier> requiredParameters,
                BiPredicate<DerivationParameter, List<DerivationParameter>> predicate) {
            this.name = name;
            this.anvilTestTemplate = anvilTestTemplate;
            this.target = target;
            this.requiredParameters = requiredParameters;
            this.predicate = predicate;
        }

        public FlexibleConditionalConstraint get() {
            return new FlexibleConditionalConstraint(
                    name, anvilTestTemplate, target, requiredParameters, predicate);
        }
    }
}
