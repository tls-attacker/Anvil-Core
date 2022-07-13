package de.rub.nds.anvilcore.context;

public interface ApplicationSpecificContextDelegate {
    default void onTestFinished(String uniqueId) {}
}
