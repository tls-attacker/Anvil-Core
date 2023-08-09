package de.rub.nds.anvilcore.coffee4j.model;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
import de.rub.nds.anvilcore.model.IpmProvider;
import de.rwth.swc.coffee4j.junit.provider.model.ModelProvider;
import de.rwth.swc.coffee4j.model.InputParameterModel;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;

/** A modified copy of the ModelFromMethodProvider from Coffee4j. */
public class ScopeBasedProvider implements ModelProvider, AnnotationConsumer<ModelFromScope> {

    private ModelFromScope modelFromScope;

    @Override
    public void accept(ModelFromScope modelFromScope) {
        this.modelFromScope = modelFromScope;
    }

    @Override
    public InputParameterModel provide(ExtensionContext extensionContext) {
        AnvilTestTemplate anvilTestTemplate = new AnvilTestTemplate(extensionContext);
        final Object providedObject = IpmProvider.generateIpm(anvilTestTemplate);
        return toInputParameterModel(providedObject);
    }

    private static InputParameterModel toInputParameterModel(Object object) {
        if (object instanceof InputParameterModel) {
            return (InputParameterModel) object;
        } else if (object instanceof InputParameterModel.Builder) {
            return ((InputParameterModel.Builder) object).build();
        } else {
            throw new JUnitException(
                    "The given method must either return an "
                            + InputParameterModel.class.getName()
                            + " or an "
                            + InputParameterModel.Builder.class.getName()
                            + ". Instead a "
                            + object.getClass().getName()
                            + " was returned");
        }
    }
}
