package de.rub.nds.anvilcore.teststate;

import org.junit.jupiter.api.extension.ExtensionContext;

public class DefaultScoreContainerFactory extends ScoreContainerFactory {
    @Override
    public ScoreContainer getInstance(ExtensionContext extensionContext) {
        return new DefaultScoreContainer();
    }
}
