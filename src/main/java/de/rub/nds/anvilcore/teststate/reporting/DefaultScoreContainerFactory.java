package de.rub.nds.anvilcore.teststate.reporting;

import org.junit.jupiter.api.extension.ExtensionContext;

public class DefaultScoreContainerFactory implements ScoreContainerFactory {
    @Override
    public ScoreContainer getInstance(ExtensionContext extensionContext) {
        return new DefaultScoreContainer();
    }

    @Override
    public ScoreContainer getInstance() {
        return new DefaultScoreContainer();
    }
}
