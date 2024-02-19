/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class SingleCheckCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        ConditionEvaluationResult evalResult = null;
        if (extensionContext.getTestMethod().isPresent()) {
            evalResult = getPreviousEvaluationResult(extensionContext);
        }
        if (evalResult == null) {
            evalResult = evaluateUncachedCondition(extensionContext);
            cacheEvalResult(extensionContext, evalResult);
            System.out.println("Creating new instance");
        } else {
            System.out.println("Using cache");
        }
        return evalResult;
    }

    protected abstract ConditionEvaluationResult evaluateUncachedCondition(
            ExtensionContext extensionContext);

    public ConditionEvaluationResult getPreviousEvaluationResult(
            ExtensionContext extensionContext) {
        ExtensionContext.Namespace namespace =
                ExtensionContext.Namespace.create(
                        extensionContext.getRequiredTestClass(),
                        extensionContext.getRequiredTestMethod());
        if (extensionContext.getStore(namespace).get(this.getClass()) == null) {
            return null;
        }
        return extensionContext
                .getStore(namespace)
                .get(this.getClass(), ConditionEvaluationResult.class);
    }

    public void cacheEvalResult(
            ExtensionContext extensionContext, ConditionEvaluationResult evalResult) {
        if (extensionContext.getTestMethod().isPresent()) {
            // only cache for test methods as containers will only be checked once
            ExtensionContext.Namespace namespace =
                    ExtensionContext.Namespace.create(
                            extensionContext.getRequiredTestClass(),
                            extensionContext.getRequiredTestMethod());
            extensionContext.getStore(namespace).put(this.getClass(), evalResult);
        }
    }

    protected ConditionEvaluationResult createInstance(ExtensionContext extensionContext) {
        ConditionEvaluationResult evalResult = null;
        if (extensionContext.getTestMethod().isPresent()) {
            // only check for methods as containers will always only be evaluated once

        }
        return evalResult;
    }
}
