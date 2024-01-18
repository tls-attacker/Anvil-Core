/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.util;

import de.rub.nds.anvilcore.annotation.AnvilTest;
import de.rub.nds.anvilcore.annotation.NonCombinatorialAnvilTest;
import java.lang.reflect.Method;

/** Determines the test ID of a Method based on the applicable annotations. */
public class TestIdResolver {
    public static String resolveTestId(Method testMethod) {
        NonCombinatorialAnvilTest nonCombinatorialAnvilTest =
                testMethod.getAnnotation(NonCombinatorialAnvilTest.class);
        AnvilTest anvilTest = testMethod.getAnnotation(AnvilTest.class);
        if (nonCombinatorialAnvilTest != null && !nonCombinatorialAnvilTest.id().isEmpty()) {
            return nonCombinatorialAnvilTest.id();
        } else if (anvilTest != null && !anvilTest.id().isEmpty()) {
            return anvilTest.id();
        }
        return testMethod.getClass().getName() + "." + testMethod.getName();
    }
}
