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
        DerivationParameter parameter = parameterFactory.getInstance(identifier);
        Assert.assertNotNull(parameter);
        Assert.assertEquals(identifier, parameter.getParameterIdentifier());
        return parameter;
    }
}
