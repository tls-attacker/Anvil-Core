package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.teststate.AnvilTestStateContainer;

public interface ApplicationSpecificContextDelegate {
    default void onTestFinished(String uniqueId, AnvilTestStateContainer finishedContainer) {}
}
