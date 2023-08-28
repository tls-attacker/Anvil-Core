/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParameterFactoryTest {

    private ParameterFactory parameterFactory;

    @BeforeEach
    public void setupTest() {
        parameterFactory = new TestParameterFactory();
        AnvilFactoryRegistry.get()
                .addParameterTypes(
                        TestDerivationParameter.TestParameterType.values(), parameterFactory);
    }

    @Test
    public void testGetInstance() {
        ParameterIdentifier identifierA =
                new ParameterIdentifier(TestDerivationParameter.TestParameterType.EXAMPLE_A);
        ParameterIdentifier identifierB =
                new ParameterIdentifier(TestDerivationParameter.TestParameterType.EXAMPLE_B);
        ParameterIdentifier identifierAlinkB =
                new ParameterIdentifier(TestDerivationParameter.TestParameterType.EXAMPLE_A);
        identifierAlinkB.setLinkedParameterIdentifier(identifierB);
        testWithIdentifier(identifierA);
        testWithIdentifier(identifierB);
        DerivationParameter linkedParameter = testWithIdentifier(identifierAlinkB);
        Assert.assertEquals(
                identifierB,
                linkedParameter.getParameterIdentifier().getLinkedParameterIdentifier());
    }

    private DerivationParameter testWithIdentifier(ParameterIdentifier identifier) {
        DerivationParameter parameter = ParameterFactory.getInstanceFromIdentifier(identifier);
        Assert.assertNotNull(parameter);
        Assert.assertEquals(identifier, parameter.getParameterIdentifier());
        return parameter;
    }
}
