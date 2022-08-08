package de.rub.nds.anvilcore.teststate.reporting;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface TestSummaryFactory {
    TestSummary getInstance(ExtensionContext extensionContext);
}
