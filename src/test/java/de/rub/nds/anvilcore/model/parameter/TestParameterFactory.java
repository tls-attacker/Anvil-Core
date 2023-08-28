/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.parameter;

public class TestParameterFactory extends ParameterFactory {

    @Override
    public DerivationParameter getInstance(ParameterIdentifier parameterIdentifier) {
        if (parameterIdentifier.getParameterType()
                == TestDerivationParameter.TestParameterType.EXAMPLE_A) {
            return new TestDerivationParameter(TestDerivationParameter.TestParameterType.EXAMPLE_A);
        } else {
            return new TestDerivationParameter(TestDerivationParameter.TestParameterType.EXAMPLE_B);
        }
    }

    @Override
    public ParameterScope resolveParameterScope(String scope) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
