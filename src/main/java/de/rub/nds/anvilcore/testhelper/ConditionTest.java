/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.testhelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ConditionTest implements ExecutionCondition {
    private static final Logger LOGGER = LogManager.getLogger();
    private Class<?>[] clazzes;

    public ConditionTest(Class<?>... clazz) {
        this.clazzes = clazz;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

        ConditionEvaluationResult result = ConditionEvaluationResult.enabled("");

        try {
            for (Class<?> i : this.clazzes) {
                ExecutionCondition cls = (ExecutionCondition) i.newInstance();
                ConditionEvaluationResult tmp = cls.evaluateExecutionCondition(context);
                if (tmp.isDisabled()) {
                    result = tmp;
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error was thrown in ConditionTest", e);
            throw new RuntimeException(e);
        }

        if (result.isDisabled()
                && context.getTestMethod().isPresent()
                && context.getRequiredTestMethod().getName().startsWith("execute")) {
            throw new RuntimeException("This test should be executed");
        }

        if (!result.isDisabled()
                && context.getTestMethod().isPresent()
                && context.getRequiredTestMethod().getName().startsWith("not_execute")) {
            throw new RuntimeException("This test should NOT be executed");
        }

        return ConditionEvaluationResult.enabled("");
    }
}
