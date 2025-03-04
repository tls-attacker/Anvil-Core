/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.annotation.methodcondition;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.rub.nds.anvilcore.annotation.MethodCondition;
import de.rub.nds.anvilcore.junit.extension.MethodConditionExtension;
import de.rub.nds.anvilcore.testhelper.ConditionTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

@MethodCondition(method = "classCondition")
public class MethodConditionAnnotationClassEnabled {

    @RegisterExtension static ConditionTest ext = new ConditionTest(MethodConditionExtension.class);

    private boolean methodConditionExecuted = false;
    private boolean classConditionExecuted = false;

    public ConditionEvaluationResult classCondition(ExtensionContext context) {
        classConditionExecuted = true;
        return ConditionEvaluationResult.enabled("");
    }

    public ConditionEvaluationResult test2(ExtensionContext context) {
        methodConditionExecuted = true;
        return ConditionEvaluationResult.enabled("");
    }

    private ConditionEvaluationResult privateEnabled(ExtensionContext context) {
        methodConditionExecuted = true;
        return ConditionEvaluationResult.enabled("");
    }

    public ConditionEvaluationResult disableC(ExtensionContext context) {
        methodConditionExecuted = true;
        return ConditionEvaluationResult.disabled("disabled");
    }

    private static ConditionEvaluationResult staticPrivateEnabled(ExtensionContext context) {
        return ConditionEvaluationResult.enabled("");
    }

    public static ConditionEvaluationResult staticPublicEnabled(ExtensionContext context) {
        return ConditionEvaluationResult.enabled("");
    }

    public ConditionEvaluationResult noParameter() {
        return ConditionEvaluationResult.enabled("");
    }

    @Test
    @MethodCondition(method = "test2")
    public void execute_validMethod() {
        assertTrue(classConditionExecuted, "Class ConditionMethod executed");
        assertTrue(methodConditionExecuted, "Method ConditionMethod executed");
    }

    @Test
    @MethodCondition(method = "staticPrivateEnabled")
    public void execute_staticPrivateEnabled() {}

    @Test
    @MethodCondition(method = "staticPublicEnabled")
    public void execute_staticPublicEnabled() {}

    @Test
    @MethodCondition(method = "privateEnabled")
    public void execute_PrivateEnabled() {
        assertTrue(classConditionExecuted, "Class ConditionMethod executed");
        assertTrue(methodConditionExecuted, "Method ConditionMethod executed");
    }

    @Test
    @MethodCondition(method = "noParameter")
    public void execute_noParameter() {}

    @Test
    @MethodCondition(method = "disableC")
    public void not_execute_validMethod_disabled() {}
}
