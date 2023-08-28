/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.annotation.methodcondition;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

class OtherClassCondition {

    public static OtherClassCondition instance;
    public boolean publicTest = false;
    public boolean privateTest = false;

    OtherClassCondition() {
        OtherClassCondition.instance = this;
    }

    public ConditionEvaluationResult publicTest(ExtensionContext context) {
        this.publicTest = true;
        return ConditionEvaluationResult.enabled("");
    }

    public ConditionEvaluationResult privateTest(ExtensionContext context) {
        this.privateTest = true;
        return ConditionEvaluationResult.enabled("");
    }
}
