/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate;

import java.util.ArrayList;
import java.util.List;

public enum TestResult {
    NOT_SPECIFIED(1, 0),
    STRICTLY_SUCCEEDED(1 << 1, 100),
    CONCEPTUALLY_SUCCEEDED(1 << 2, 80),
    PARTIALLY_FAILED(1 << 3, 20),
    FULLY_FAILED(1 << 4, 0),
    DISABLED(1 << 5, 0),
    TEST_SUITE_ERROR(1 << 6, 0);

    private final int value;
    private final int scorePercentage;

    TestResult(int value, int scorePercentage) {
        this.value = value;
        this.scorePercentage = scorePercentage;
    }

    public int getValue() {
        return value;
    }

    public static List<TestResult> parse(int val) {
        List<TestResult> resultList = new ArrayList<TestResult>();
        if (val == 0) {
            resultList.add(NOT_SPECIFIED);
            return resultList;
        }

        for (TestResult ap : values()) {
            if ((val & ap.getValue()) > 0) resultList.add(ap);
        }
        return resultList;
    }

    public static TestResult resultForBitmask(int val) {
        if (val == 0) {
            return NOT_SPECIFIED;
        }

        List<TestResult> result = TestResult.parse(val);
        if (result.size() == 1) {
            return result.get(0);
        }

        if (result.contains(DISABLED)) {
            throw new UnsupportedOperationException("TestResult Bitmask contains DISABLED");
        }

        if (result.contains(NOT_SPECIFIED)) {
            throw new UnsupportedOperationException("TestResult Bitmask contains NOT_SPECIFIED");
        }

        if (result.contains(FULLY_FAILED) || result.contains(PARTIALLY_FAILED)) {
            if (result.contains(CONCEPTUALLY_SUCCEEDED) || result.contains(STRICTLY_SUCCEEDED)) {
                return PARTIALLY_FAILED;
            }
        }

        return result.get(result.size() - 1);
    }

    public int getScorePercentage() {
        return scorePercentage;
    }
}
