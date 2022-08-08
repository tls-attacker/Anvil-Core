package de.rub.nds.anvilcore.teststate.reporting;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface ScoreContainerFactory {
    ScoreContainer getInstance(ExtensionContext extensionContext);
}
