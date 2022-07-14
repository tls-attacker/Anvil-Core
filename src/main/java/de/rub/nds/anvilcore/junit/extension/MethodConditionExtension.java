package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.annotation.MethodCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodConditionExtension implements ExecutionCondition {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        Class<?> requiredTestClass = extensionContext.getRequiredTestClass();

        if (extensionContext.getTestMethod().isEmpty()) {
            return ConditionEvaluationResult.enabled("");
        }
        Method testMethod = extensionContext.getTestMethod().get();


        Method classConditionMethod = null;    // From test class annotation
        Method methodConditionMethod = null;   // From test method annotation
        if (requiredTestClass.isAnnotationPresent(MethodCondition.class)) {
            MethodCondition methodConditionAnnotation = requiredTestClass.getAnnotation(MethodCondition.class);
            classConditionMethod = getMethodForAnnotation(methodConditionAnnotation, requiredTestClass);
        }
        if (testMethod.isAnnotationPresent(MethodCondition.class)) {
            MethodCondition methodConditionAnnotation = testMethod.getAnnotation(MethodCondition.class);
            methodConditionMethod = getMethodForAnnotation(methodConditionAnnotation, requiredTestClass);
        }

        if (methodConditionMethod != null) {
            ConditionEvaluationResult evaluationResult = invokeMethod(methodConditionMethod, extensionContext);
            if (!evaluationResult.isDisabled() && classConditionMethod != null) {
                evaluationResult = invokeMethod(methodConditionMethod, extensionContext);
            }
            return evaluationResult;
        }
        else if (classConditionMethod != null) {
            return invokeMethod(classConditionMethod, extensionContext);
        }
        else {
            return ConditionEvaluationResult.enabled("No MethodCondition found");
        }

    }

    private Method getMethodForAnnotation(MethodCondition methodCondition, Class<?> testClass) {
        if (!methodCondition.clazz().equals(Object.class)) {
            return resolveMethod(testClass, methodCondition.method());
        }
        else {
            return resolveMethod(methodCondition.clazz(), methodCondition.method());
        }
    }

    private Method resolveMethod(Class<?> clazz, String method) {
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

    private ConditionEvaluationResult invokeMethod(Method method, ExtensionContext extensionContext) {
        try {
            method.setAccessible(true);
            // Retrieve or construct instance of class that contains method
            Object classInstance;
            if (extensionContext.getTestInstance().isPresent()
                    && method.getDeclaringClass().equals(extensionContext.getTestInstance().get())) {
                classInstance = extensionContext.getTestInstance().get();
            }
            else {
                Class<?> clazz = method.getDeclaringClass();
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                classInstance = constructor.newInstance();
            }

            Object returnValue;
            if (method.getParameterCount() > 0 ) {
                returnValue = method.invoke(classInstance, extensionContext);
            }
            else {
                returnValue = method.invoke(classInstance);
            }

            if (!returnValue.getClass().equals(ConditionEvaluationResult.class)) {
                LOGGER.error("Method specified in MethodCondition annotation must return a ConditionalEvaluationResult object");
                return ConditionEvaluationResult.disabled("Invalid return type of " + method.getName());
            }
            return (ConditionEvaluationResult) returnValue;
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOGGER.error("Unable to invoke method specified in MethodCondition", e);
            return ConditionEvaluationResult.disabled("Unable to invoke method specified in MethodCondition");
        }
    }
}
