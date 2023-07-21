/*
 * Copyright 2023 marcel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rub.nds.anvilcore.model.parameter;

import de.rub.nds.anvilcore.model.DerivationScope;
import java.util.LinkedList;
import java.util.List;

/**
 * @author marcel
 */
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
