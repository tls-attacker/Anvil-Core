package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.TestContext;

public abstract class ParameterFactory {
    public abstract DerivationParameter getInstance(ParameterIdentifier parameterIdentifier);

    public abstract ParameterScope resolveParameterScope(String scope);

    public static DerivationParameter getInstanceFromIdentifier(ParameterIdentifier parameterIdentifier) {
        ParameterFactory parameterFactory = TestContext.getInstance().getParameterFactory(parameterIdentifier.getParameterType());
        return parameterFactory.getInstance(parameterIdentifier);
    }
}
