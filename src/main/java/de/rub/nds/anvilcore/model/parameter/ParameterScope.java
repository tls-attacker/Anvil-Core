package de.rub.nds.anvilcore.model.parameter;

public abstract class ParameterScope {
    public static final ParameterScope NO_SCOPE = new NoParameterScope();

    public abstract String getUniqueScopeIdentifier();

    @Override
    public String toString() {
        return getUniqueScopeIdentifier();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ParameterScope)) {
            return false;
        }
        ParameterScope other = (ParameterScope) obj;
        return this.getUniqueScopeIdentifier().equals(other.getUniqueScopeIdentifier());
    }

    @Override
    public int hashCode() {
        return this.getUniqueScopeIdentifier().hashCode();
    }

    private static class NoParameterScope extends ParameterScope {
        @Override
        public String getUniqueScopeIdentifier() {
            return "_";
        }
    }
}
