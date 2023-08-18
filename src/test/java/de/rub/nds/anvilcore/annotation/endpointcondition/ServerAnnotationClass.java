/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
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

@ServerTest
public class ServerAnnotationClass {

    @RegisterExtension
    static ConditionTest ext = new ConditionTest(EndpointConditionExtension.class);

    @BeforeAll
    public static void setEvaluatedEndpoint() {
        AnvilContext.getInstance().getConfig().setEndpointMode(TestEndpointType.CLIENT);
    }

    @ClientTest
    public void execute_supportedForConfig() {}

    @ServerTest
    public void not_execute_unsupportedModeForConfig() {}
}
