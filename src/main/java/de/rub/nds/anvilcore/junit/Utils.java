/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.annotation.AnvilTest;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;

public class Utils {

    public static ExtensionContext getTemplateContainerExtensionContext(
            ExtensionContext extensionContext) {
        if (!extensionContextIsBasedOnCombinatorialTesting(extensionContext)) {
            return extensionContext;
        } else {
            Optional<ExtensionContext> tmp = extensionContext.getParent();
            while (tmp.isPresent()) {
                if (extensionContextIsBasedOnCombinatorialTesting(tmp.get())) {
                    return tmp.get();
                }
                tmp = tmp.get().getParent();
            }
            return extensionContext;
        }
    }

    public static boolean extensionContextIsBasedOnCombinatorialTesting(
            ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        // this will also yield false for all disabled tests
        return testMethod.isPresent() && testMethod.get().isAnnotationPresent(AnvilTest.class);
    }
}
