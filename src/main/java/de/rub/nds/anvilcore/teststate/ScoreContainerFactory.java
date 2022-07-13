package de.rub.nds.anvilcore.teststate;

import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class ScoreContainerFactory {
    public abstract ScoreContainer getInstance(ExtensionContext extensionContext);
}
