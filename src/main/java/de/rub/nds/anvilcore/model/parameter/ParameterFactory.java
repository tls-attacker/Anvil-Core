package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;

public abstract class ParameterFactory {
    protected abstract DerivationParameter getInstance(ParameterIdentifier parameterIdentifier);

    public abstract ParameterScope resolveParameterScope(String scope);

    public static DerivationParameter getInstanceFromIdentifier(
            ParameterIdentifier parameterIdentifier) {
        ParameterFactory parameterFactory =
                AnvilFactoryRegistry.get()
                        .getParameterFactory(parameterIdentifier.getParameterType());
        DerivationParameter newParameter = parameterFactory.getInstance(parameterIdentifier);
        if (!newParameter.getParameterIdentifier().hasLinkedParameterIdentifier()
                && parameterIdentifier.hasLinkedParameterIdentifier()) {
            newParameter
                    .getParameterIdentifier()
                    .setLinkedParameterIdentifier(
                            parameterIdentifier.getLinkedParameterIdentifier());
        }
        return newParameter;
    }
}
