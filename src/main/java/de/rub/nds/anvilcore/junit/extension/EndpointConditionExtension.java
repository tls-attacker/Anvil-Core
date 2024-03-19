/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.annotation.ClientTest;
import de.rub.nds.anvilcore.annotation.ServerTest;
import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.context.AnvilContext;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Evaluates the ClientTest, ServerTest, TestEndpoint annotations The test is disabled when a client
 * is tested but the test is written for servers.
 */
public class EndpointConditionExtension extends SingleCheckCondition {

    private TestEndpointType endpointOfMethod(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();

        return endpointOfMethod(testMethod, testClass);
    }

    public static TestEndpointType endpointOfMethod(Method testMethod, Class<?> testClass) {
        if (testMethod.isAnnotationPresent(ClientTest.class)) {
            return TestEndpointType.CLIENT;
        } else if (testMethod.isAnnotationPresent(ServerTest.class)) {
            return TestEndpointType.SERVER;
        } else if (testClass.isAnnotationPresent(ClientTest.class)) {
            return TestEndpointType.CLIENT;
        } else if (testClass.isAnnotationPresent(ServerTest.class)) {
            return TestEndpointType.SERVER;
        } else {
            return TestEndpointType.BOTH;
        }
    }

    @Override
    public ConditionEvaluationResult evaluateUncachedCondition(ExtensionContext extensionContext) {
        if (!extensionContext.getTestMethod().isPresent()) {
            return ConditionEvaluationResult.enabled("Class annotations are not relevant.");
        }

        AnvilContext anvilContext = AnvilContext.getInstance();
        TestEndpointType targetEndpoint = endpointOfMethod(extensionContext);

        if (targetEndpoint.isMatchingTestEndpointType(anvilContext.getConfig().getEndpointMode())) {
            return ConditionEvaluationResult.enabled("TestEndpointMode matches");
        }

        return ConditionEvaluationResult.disabled("TestEndpointMode doesn't match");
    }
}
