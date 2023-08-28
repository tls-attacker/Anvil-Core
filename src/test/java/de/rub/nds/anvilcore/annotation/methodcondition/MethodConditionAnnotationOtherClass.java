/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.annotation.methodcondition;

import static org.junit.Assert.assertTrue;

import de.rub.nds.anvilcore.annotation.MethodCondition;
import de.rub.nds.anvilcore.junit.extension.MethodConditionExtension;
import de.rub.nds.anvilcore.testhelper.ConditionTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MethodConditionAnnotationOtherClass {

    @RegisterExtension static ConditionTest ext = new ConditionTest(MethodConditionExtension.class);

    @Test
    @MethodCondition(clazz = OtherClassCondition.class, method = "publicTest")
    public void execute_validPublicMethod() {
        assertTrue(OtherClassCondition.instance.publicTest);
    }

    @Test
    @MethodCondition(clazz = OtherClassCondition.class, method = "privateTest")
    public void execute_validPrivateMethod() {
        assertTrue(OtherClassCondition.instance.privateTest);
    }

    @Test
    public void execute_noAnnotation() {}
}
