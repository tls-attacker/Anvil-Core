/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.coffee4j.junit;

import de.rwth.swc.coffee4j.junit.CombinatorialTestExecutionCallback;
import de.rwth.swc.coffee4j.junit.CombinatorialTestMethodContext;
import de.rwth.swc.coffee4j.junit.CombinatorialTestNameFormatter;
import de.rwth.swc.coffee4j.model.Combination;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

public class AnvilCombinatorialTestInvocationContext implements TestTemplateInvocationContext {
    private final CombinatorialTestNameFormatter nameFormatter;
    private final CombinatorialTestMethodContext methodContext;
    private final Combination testInput;

    public AnvilCombinatorialTestInvocationContext(
            CombinatorialTestNameFormatter nameFormatter,
            CombinatorialTestMethodContext methodContext,
            Combination testInput) {
        this.nameFormatter = nameFormatter;
        this.methodContext = methodContext;
        this.testInput = testInput;
    }

    public String getDisplayName(int invocationIndex) {
        return this.nameFormatter.format(invocationIndex, this.testInput);
    }

    public List<Extension> getAdditionalExtensions() {
        return Arrays.asList(
                new TestCaseCreator(this.testInput),
                new TestCaseResolver(this.methodContext, this.testInput),
                new CombinatorialTestExecutionCallback(this.testInput));
    }
}
