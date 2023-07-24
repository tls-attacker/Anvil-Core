package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.teststate.AnvilTestRun;

public interface ApplicationSpecificContextDelegate {
    default void onTestFinished(String uniqueId, AnvilTestRun finishedContainer) {}
}
