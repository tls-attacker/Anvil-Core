package de.rub.nds.anvilcore.teststate.reporting;

import de.rub.nds.anvilcore.teststate.TestResult;

public interface ScoreContainer {
    void updateForResult(TestResult result);
    void merge(ScoreContainer other);
}
