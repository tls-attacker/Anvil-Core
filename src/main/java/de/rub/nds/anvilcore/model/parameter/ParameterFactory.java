package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.AnvilContext;

public abstract class ParameterFactory {
    public abstract DerivationParameter getInstance(ParameterIdentifier parameterIdentifier);

    public abstract ParameterScope resolveParameterScope(String scope);

    public static DerivationParameter getInstanceFromIdentifier(ParameterIdentifier parameterIdentifier) {
        ParameterFactory parameterFactory = AnvilContext.getInstance().getParameterFactory(parameterIdentifier.getParameterType());
        return parameterFactory.getInstance(parameterIdentifier);
    }
}
