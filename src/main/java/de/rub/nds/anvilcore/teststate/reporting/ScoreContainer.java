/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonValue;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.teststate.TestResult;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreContainer {

    private static final Logger LOGGER = LogManager.getLogger();

    @JsonValue Map<String, Score> scoreMap = new HashMap<>();

    public ScoreContainer() {}

    public ScoreContainer(String testId) {
        Map<String, Integer> severityLevels =
                AnvilContext.getInstance().getMetadataFetcher().getTestSeverityLevels(testId);
        if (severityLevels == null) throw new RuntimeException("Metadata not found!");
        for (Map.Entry<String, Integer> t : severityLevels.entrySet()) {
            scoreMap.put(t.getKey(), new Score(t.getValue()));
        }
    }

    public Map<String, Score> getScoreMap() {
        return scoreMap;
    }

    public void updateForResult(TestResult result) {
        for (Score i : scoreMap.values()) {
            i.updateForTestResult(result);
        }
    }

    public void merge(ScoreContainer other) {
        for (String i : other.getScoreMap().keySet()) {
            Score toUpdate = this.scoreMap.get(i);
            if (toUpdate == null) {
                toUpdate = new Score();
                this.scoreMap.put(i, toUpdate);
            }
            toUpdate.setReached(toUpdate.getReached() + other.getScoreMap().get(i).getReached());
            toUpdate.setTotal(toUpdate.getTotal() + other.getScoreMap().get(i).getTotal());
        }
    }
}
