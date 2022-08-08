package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.annotation.TestChooser;
import de.rub.nds.anvilcore.coffee4j.junit.AnvilCombinatorialTestExtension;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.IpmProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public class TestChooserExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        if (!extensionContext.getTestMethod().isPresent()) {
            return false;
        }

        Method testMethod = extensionContext.getRequiredTestMethod();
        if (!AnnotationUtils.isAnnotated(testMethod, TestChooser.class)) {
            return false;
        }

        DerivationScope derivationScope = new DerivationScope(extensionContext);
        if (IpmProvider.mustUseSimpleModel(derivationScope)) {
            // FIXME
            // return new SimpleTestExtension().supportsTestTemplate(extensionContext);
            return false;
        } else {
            return new AnvilCombinatorialTestExtension().supportsTestTemplate(extensionContext);
        }
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        DerivationScope scope = new DerivationScope(extensionContext);
        if(IpmProvider.mustUseSimpleModel(scope)) {
            // FIXME
            //return new SimpleTestExtension().provideTestTemplateInvocationContexts(extensionContext);
            return null;
        } else {
            return new AnvilCombinatorialTestExtension().provideTestTemplateInvocationContexts(extensionContext);
        }
    }
}
