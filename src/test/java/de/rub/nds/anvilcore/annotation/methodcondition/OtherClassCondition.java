/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
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
