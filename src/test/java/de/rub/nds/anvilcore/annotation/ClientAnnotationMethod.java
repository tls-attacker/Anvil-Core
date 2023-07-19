/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.anvilcore.annotation;

import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.junit.extension.EndpointConditionExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ClientAnnotationMethod {

    @RegisterExtension
    static ConditionTest ext = new ConditionTest(EndpointConditionExtension.class);

    @BeforeAll
    public static void setEvaluatedEndpoint() {
        AnvilContext.getInstance().setEvaluatedEndpoint(TestEndpointType.CLIENT);
    }

    @ClientTest
    public void execute_supported() {}

    @ServerTest
    public void not_execute_unsupportedForConfig() {}
}
