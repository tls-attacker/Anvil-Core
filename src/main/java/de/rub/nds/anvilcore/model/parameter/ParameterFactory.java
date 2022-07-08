package de.rub.nds.anvilcore.model.parameter;

public abstract class ParameterFactory {
    public abstract DerivationParameter getInstance(ParameterIdentifier parameterIdentifier);

    public abstract ParameterScope resolveParameterScope(String scope);
}
