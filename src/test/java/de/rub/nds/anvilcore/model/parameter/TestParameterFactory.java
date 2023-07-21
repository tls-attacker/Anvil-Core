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
