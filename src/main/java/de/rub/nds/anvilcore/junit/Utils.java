package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.annotation.AnvilTest;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;

public class Utils {

    public static ExtensionContext getTemplateContainerExtensionContext(ExtensionContext extensionContext) {
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

    public static boolean extensionContextIsBasedOnCombinatorialTesting(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        //this will also yield false for all disabled tests
        return testMethod.isPresent() && testMethod.get().isAnnotationPresent(AnvilTest.class);
    }
}
