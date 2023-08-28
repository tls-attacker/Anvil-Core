/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.annotation.endpointcondition;

import de.rub.nds.anvilcore.annotation.ClientTest;
import de.rub.nds.anvilcore.annotation.ServerTest;
import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.extension.EndpointConditionExtension;
import de.rub.nds.anvilcore.testhelper.ConditionTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

@ClientTest
public class ClientAnnotationClassServerTest {

    @RegisterExtension
    static ConditionTest ext = new ConditionTest(EndpointConditionExtension.class);

    @BeforeAll
    public static void setEvaluatedEndpoint() {
        AnvilContext.getInstance().setEvaluatedEndpoint(TestEndpointType.SERVER);
    }

    @ClientTest
    public void not_execute_unsupportedForConfig() {}

    @ServerTest
    public void execute_supportedModeForConfig() {}
}
