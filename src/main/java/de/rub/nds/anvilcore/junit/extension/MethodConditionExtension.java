/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.annotation.MethodCondition;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MethodConditionExtension extends SingleCheckCondition {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public ConditionEvaluationResult evaluateUncachedCondition(ExtensionContext extensionContext) {
        ConditionEvaluationResult evalResult = createInstance(extensionContext);
        if (evalResult == null) {
            Class<?> requiredTestClass = extensionContext.getRequiredTestClass();

            if (extensionContext.getTestMethod().isEmpty()) {
                evalResult = ConditionEvaluationResult.enabled("");
            } else {
                Method testMethod = extensionContext.getTestMethod().get();

                Method classConditionMethod = null; // From test class annotation
                Method methodConditionMethod = null; // From test method annotation
                if (requiredTestClass.isAnnotationPresent(MethodCondition.class)) {
                    MethodCondition methodConditionAnnotation =
                            requiredTestClass.getAnnotation(MethodCondition.class);
                    classConditionMethod =
                            getMethodForAnnotation(methodConditionAnnotation, requiredTestClass);
                }
                if (testMethod.isAnnotationPresent(MethodCondition.class)) {
                    MethodCondition methodConditionAnnotation =
                            testMethod.getAnnotation(MethodCondition.class);
                    methodConditionMethod =
                            getMethodForAnnotation(methodConditionAnnotation, requiredTestClass);
                }

                if (methodConditionMethod == null && classConditionMethod == null) {
                    evalResult = ConditionEvaluationResult.enabled("No MethodCondition found");
                } else {

                    if (methodConditionMethod != null) {
                        evalResult = invokeMethod(methodConditionMethod, extensionContext);
                    }

                    if (classConditionMethod != null
                            && (evalResult == null || !evalResult.isDisabled())) {
                        evalResult = invokeMethod(classConditionMethod, extensionContext);
                    }
                }
            }
            cacheEvalResult(extensionContext, evalResult);
            return evalResult;

        } else {
            return evalResult;
        }
    }

    public static Method getMethodForAnnotation(
            MethodCondition methodCondition, Class<?> testClass) {
        if (methodCondition.clazz().equals(Object.class)) {
            return resolveMethod(testClass, methodCondition.method());
        } else {
            return resolveMethod(methodCondition.clazz(), methodCondition.method());
        }
    }

    public static Method resolveMethod(Class<?> clazz, String method) {
        try {
            Arrays.stream(clazz.getDeclaredMethods()).forEach(m -> m.setAccessible(true));
            try {
                return clazz.getDeclaredMethod(method, ExtensionContext.class);
            } catch (NoSuchMethodException e) {
                return clazz.getDeclaredMethod(method);
            }
        } catch (Exception e) {
            LOGGER.error("Method declared in MethodCondition annotation not found");
            throw new RuntimeException("Method declared in MethodCondition annotation not found");
        }
    }

    private ConditionEvaluationResult invokeMethod(
            Method method, ExtensionContext extensionContext) {
        try {
            method.setAccessible(true);
            // Retrieve or construct instance of class that contains method
            Object classInstance;
            if (extensionContext.getTestInstance().isPresent()
                    && method.getDeclaringClass().equals(extensionContext.getRequiredTestClass())) {
                classInstance = extensionContext.getTestInstance().get();
            } else {
                Class<?> clazz = method.getDeclaringClass();
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                classInstance = constructor.newInstance();
            }

            Object returnValue;
            if (method.getParameterCount() > 0) {
                returnValue = method.invoke(classInstance, extensionContext);
            } else {
                returnValue = method.invoke(classInstance);
            }

            if (!returnValue.getClass().equals(ConditionEvaluationResult.class)) {
                LOGGER.error(
                        "Method specified in MethodCondition annotation must return a ConditionalEvaluationResult object");
                return ConditionEvaluationResult.disabled(
                        "Invalid return type of " + method.getName());
            }
            return (ConditionEvaluationResult) returnValue;
        } catch (NoSuchMethodException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException e) {
            LOGGER.error("Unable to invoke method specified in MethodCondition", e);
            return ConditionEvaluationResult.disabled(
                    "Unable to invoke method specified in MethodCondition");
        }
    }
}
