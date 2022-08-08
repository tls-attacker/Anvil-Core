package de.rub.nds.anvilcore.teststate.reporting;

import org.junit.jupiter.api.extension.ExtensionContext;

public class DefaultTestSummaryFactory implements TestSummaryFactory{
    @Override
    public TestSummary getInstance(ExtensionContext extensionContext) {
        return new TestSummary();
    }
}
