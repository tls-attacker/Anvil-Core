package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.rub.nds.anvilcore.teststate.TestResult;

public class DefaultScoreContainer implements ScoreContainer {
    // TODO Create reasonable default implementation

    @JsonProperty("Score")
    String todo = "todo";   // TODO

    @Override
    public void updateForResult(TestResult result) {}

    @Override
    public void merge(ScoreContainer other) {

    }
}
