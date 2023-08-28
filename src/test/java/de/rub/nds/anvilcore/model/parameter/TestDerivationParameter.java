/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.model.DerivationScope;
import java.util.LinkedList;
import java.util.List;

public class TestDerivationParameter extends DerivationParameter<TestAnvilConfig, Integer> {

    private final TestParameterType testParameterType;

    public TestDerivationParameter(TestParameterType testParameterType) {
        super(Integer.class, TestAnvilConfig.class, new ParameterIdentifier(testParameterType));
        this.testParameterType = testParameterType;
    }

    public TestDerivationParameter(TestParameterType testParameterType, Integer value) {
        this(testParameterType);
        setSelectedValue(value);
    }

    @Override
    public void applyToConfig(TestAnvilConfig config, DerivationScope derivationScope) {}

    @Override
    public List<DerivationParameter<TestAnvilConfig, Integer>> getParameterValues(
            DerivationScope derivationScope) {
        List<DerivationParameter<TestAnvilConfig, Integer>> valueList = new LinkedList<>();
        valueList.add(new TestDerivationParameter(testParameterType, 1));
        valueList.add(new TestDerivationParameter(testParameterType, 2));
        valueList.add(new TestDerivationParameter(testParameterType, 3));
        return valueList;
    }

    @Override
    protected DerivationParameter<TestAnvilConfig, Integer> generateValue(Integer selectedValue) {
        return new TestDerivationParameter(testParameterType, selectedValue);
    }

    public enum TestParameterType implements ParameterType {
        EXAMPLE_A,
        EXAMPLE_B
    }
}
