package de.rub.nds.anvilcore.model.parameter;

import java.util.Objects;

public class ParameterIdentifier {
    private final ParameterType parameterType;
    private final ParameterScope parameterScope;

    public ParameterIdentifier(ParameterType parameterType, ParameterScope parameterScope) {
        this.parameterType = parameterType;
        this.parameterScope = parameterScope;
    }

    public ParameterIdentifier(ParameterType parameterType) {
        this.parameterType = parameterType;
        this.parameterScope = ParameterScope.NO_SCOPE;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public ParameterScope getParameterScope() {
        return parameterScope;
    }

    @Override
    public String toString() {
        if (parameterScope == ParameterScope.NO_SCOPE || parameterScope == null) {
            return parameterType.toString().toLowerCase();
        }
        return parameterScope + "." + parameterType.toString().toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ParameterIdentifier)) {
            return false;
        }
        ParameterIdentifier other = (ParameterIdentifier) obj;
        return this.parameterType.equals(other.parameterType) && this.parameterScope.equals(other.parameterScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameterScope, this.parameterType);
    }
}
