package de.rub.nds.anvilcore.junit;

import de.rub.nds.anvilcore.annotation.TestChooser;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.IpmFactory;
import de.rwth.swc.coffee4j.junit.CombinatorialTestExtension;
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
        if (IpmFactory.mustUseSimpleModel(derivationScope)) {
            // FIXME
            // return new SimpleTestExtension().supportsTestTemplate(extensionContext);
            return false;
        } else {
            // TODO: Might need to implement own CombinatorialTestExtension like in TLS-Anvil
            return new CombinatorialTestExtension().supportsTestTemplate(extensionContext);
        }
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        DerivationScope scope = new DerivationScope(extensionContext);
        if(IpmFactory.mustUseSimpleModel(scope)) {
            // FIXME
            //return new SimpleTestExtension().provideTestTemplateInvocationContexts(extensionContext);
            return null;
        } else {
            // TODO: Might need to implement own CombinatorialTestExtension like in TLS-Anvil
            return new CombinatorialTestExtension().provideTestTemplateInvocationContexts(extensionContext);
        }
    }
}
