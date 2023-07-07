package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;
import java.util.Objects;

public class ParameterIdentifier {
    private final ParameterType parameterType;
    private final ParameterScope parameterScope;

    // references another parameter that *must* be modeled along with this one
    private ParameterIdentifier linkedParameterIdentifier;

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

    public String name() {
        if (parameterScope == ParameterScope.NO_SCOPE || parameterScope == null) {
            return parameterType.toString();
        }
        return parameterScope + "." + parameterType.toString();
    }

    public static ParameterIdentifier fromName(String name) {
        if (!name.contains(".")) {
            // No parameter scope
            return new ParameterIdentifier(ParameterType.resolveParameterType(name));
        } else {
            String scopeName = name.substring(0, name.lastIndexOf("."));
            String typeName = name.substring(name.lastIndexOf(".") + 1);
            ParameterType parameterType = ParameterType.resolveParameterType(typeName);
            ParameterScope parameterScope =
                    AnvilFactoryRegistry.get()
                            .getParameterFactory(parameterType)
                            .resolveParameterScope(scopeName);
            return new ParameterIdentifier(parameterType, parameterScope);
        }
    }

    @Override
    public String toString() {
        return name();
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
        return this.parameterType.equals(other.parameterType)
                && this.parameterScope.equals(other.parameterScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameterScope, this.parameterType);
    }

    public ParameterIdentifier getLinkedParameterIdentifier() {
        return linkedParameterIdentifier;
    }

    public void setLinkedParameterIdentifier(ParameterIdentifier linkedParameterIdentifier) {
        this.linkedParameterIdentifier = linkedParameterIdentifier;
    }

    public boolean hasLinkedParameterIdentifier() {
        return linkedParameterIdentifier != null;
    }
}
