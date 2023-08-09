package de.rub.nds.anvilcore.model.parameter;

/**
 * Represents the properties affected by the test derivation models. This class should be
 * implemented as enum.
 */
public interface ParameterType {

    DerivationParameter getInstance(ParameterScope scope);
}
