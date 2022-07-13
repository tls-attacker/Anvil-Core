package de.rub.nds.anvilcore.junit.simpletest;

import de.rub.nds.anvilcore.annotation.TestChooser;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.IpmFactory;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

public class SimpleTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        if (!extensionContext.getTestMethod().isPresent()) {
            return false;
        }

        Method testMethod = extensionContext.getRequiredTestMethod();
        if (!isAnnotated(testMethod, TestChooser.class)) {
            return false;
        }

        return IpmFactory.mustUseSimpleModel(new DerivationScope(extensionContext));
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        DerivationScope derivationScope = new DerivationScope(extensionContext);
        List<DerivationParameter> dynamicParameterValues = (List<DerivationParameter>) IpmFactory.getSimpleModelVariations(derivationScope);
        SimpleTestManagerContainer managerContainer = SimpleTestManagerContainer.getInstance();
        if(dynamicParameterValues != null) {
            managerContainer.addManagerByExtensionContext(extensionContext, dynamicParameterValues.size());
            return dynamicParameterValues.stream().map(value -> new SimpleTestInvocationContext(value));
        } else {
            managerContainer.addManagerByExtensionContext(extensionContext, 1);
            return Stream.of(new SimpleTestInvocationContext());
        }
    }
}
