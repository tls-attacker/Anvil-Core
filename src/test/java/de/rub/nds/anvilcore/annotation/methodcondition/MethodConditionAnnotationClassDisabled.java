/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.annotation.methodcondition;

import de.rub.nds.anvilcore.annotation.MethodCondition;
import de.rub.nds.anvilcore.junit.extension.MethodConditionExtension;
import de.rub.nds.anvilcore.testhelper.ConditionTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

@MethodCondition(method = "classCondition")
public class MethodConditionAnnotationClassDisabled {

    @RegisterExtension static ConditionTest ext = new ConditionTest(MethodConditionExtension.class);

    public ConditionEvaluationResult classCondition(ExtensionContext context) {
        return ConditionEvaluationResult.disabled("");
    }

    public ConditionEvaluationResult test2(ExtensionContext context) {
        return ConditionEvaluationResult.enabled("");
    }

    public ConditionEvaluationResult disableC(ExtensionContext context) {
        return ConditionEvaluationResult.disabled("disabled");
    }

    public ConditionEvaluationResult noParameter() {
        return ConditionEvaluationResult.enabled("");
    }

    @Test
    @MethodCondition(method = "test2")
    public void not_execute_validMethod() {}

    @Test
    @MethodCondition(method = "disableC")
    public void not_execute_validMethod_disabled() {}

    @Test
    @MethodCondition(method = "noParameter")
    public void not_execute_noParameter() {}
}
