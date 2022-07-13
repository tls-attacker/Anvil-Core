/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.anvilcore.junit.simpletest;

import de.rub.nds.anvilcore.coffee4j.junit.AnvilCombinatorialTestNameFormatter;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class SimpleTestInvocationContext implements TestTemplateInvocationContext {
    private final AnvilCombinatorialTestNameFormatter nameFormatter;
    private final List<DerivationParameter> testInput;

    public SimpleTestInvocationContext(DerivationParameter testInput) {
        this();
        this.testInput.add(testInput);
    } 
    
    public SimpleTestInvocationContext() {
        this.testInput = new LinkedList<>();
        this.nameFormatter = new AnvilCombinatorialTestNameFormatter("[{index}] {combination}");
    }
    
    @Override
    public String getDisplayName(int invocationIndex) {
        return nameFormatter.format(invocationIndex, testInput);
        
    }
    
    @Override
    public List<Extension> getAdditionalExtensions() {
        return Arrays.asList(new SimpleTestParameterResolver(testInput), new SimpleTestExecutionCallback());
    }
}
