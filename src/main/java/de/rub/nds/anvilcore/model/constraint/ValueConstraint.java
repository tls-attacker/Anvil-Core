/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.constraint;

import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;

public class ValueConstraint {
    private final ParameterIdentifier affectedParameter;
    private final String evaluationMethod;
    private final Class<?> clazz;
    private final boolean dynamic;

    public ValueConstraint(
            ParameterIdentifier affectedParameter,
            String evaluationMethod,
            Class<?> clazz,
            boolean dynamic) {
        this.affectedParameter = affectedParameter;
        this.evaluationMethod = evaluationMethod;
        this.clazz = clazz;
        this.dynamic = dynamic;
    }

    public ParameterIdentifier getAffectedParameter() {
        return affectedParameter;
    }

    public String getEvaluationMethod() {
        return evaluationMethod;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public boolean isDynamic() {
        return dynamic;
    }
}
