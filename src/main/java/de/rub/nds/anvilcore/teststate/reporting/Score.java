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
import de.rub.nds.anvilcore.teststate.TestResult;

public class Score {

    private double reached = 0;
    private double total = 0;

    public Score() {}

    public Score(double level) {
        this(0, level);
    }

    public Score(double reached, double total) {
        this.reached = reached;
        this.total = total;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getReached() {
        return reached;
    }

    public void setReached(double reached) {
        this.reached = reached;
    }

    @JsonValue
    public double getPercentage() {
        if (total == 0) return 100;
        // rounded to two decimal places
        return Math.round(reached / total * 10000) / 100.0;
    }

    public void updateForTestResult(TestResult result) {
        if (result == TestResult.DISABLED) {
            setReached(0);
            setTotal(0);
            return;
        }
        setReached((result.getScorePercentage() / 100.0) * total);
    }
}
