/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.teststate.TestResult;
import java.util.Date;
import java.util.HashSet;

public class AnvilReport {
    @JsonProperty("ElapsedTime")
    private long elapsedTime;

    @JsonProperty("Identifier")
    private String identifier;

    @JsonProperty("Date")
    private Date date;

    @JsonProperty("TotalTests")
    private long totalTests;

    @JsonProperty("FinishedTests")
    private long finishedTests;

    @JsonProperty("StrictlySucceededTests")
    private long testsStrictlySucceeded;

    @JsonProperty("ConceptuallySucceededTests")
    private long testsConceptuallySucceeded;

    @JsonProperty("DisabledTests")
    private long testsDisabled;

    @JsonProperty("PartiallyFailedTests")
    private long testsPartiallyFailed;

    @JsonProperty("FullyFailedTests")
    private long testsFullyFailed;

    @JsonProperty("TestSuiteErrorTests")
    private long testsTestSuiteError;

    @JsonProperty("AdditionalConfig")
    private String configString;

    @JsonProperty("AnvilConfig")
    private String anvilConfigString;

    @JsonProperty("TestCaseCount")
    private long testCaseCount;

    @JsonProperty("TestEndpointType")
    private TestEndpointType endpointType;

    @JsonProperty("Running")
    private boolean running;

    @JsonProperty("Score")
    @JsonUnwrapped
    private ScoreContainer scoreContainer;

    public AnvilReport(AnvilContext context, boolean running) {
        this.elapsedTime = System.currentTimeMillis() - context.getCreationTime().getTime();
        this.identifier = context.getConfig().getIdentifier();
        this.date = context.getCreationTime();
        this.testsDisabled =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.DISABLED, k -> new HashSet<>())
                        .size();
        this.testsFullyFailed =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.FULLY_FAILED, k -> new HashSet<>())
                        .size();
        this.testsStrictlySucceeded =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.STRICTLY_SUCCEEDED, k -> new HashSet<>())
                        .size();
        this.testsPartiallyFailed =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.PARTIALLY_FAILED, k -> new HashSet<>())
                        .size();
        this.testsConceptuallySucceeded =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.CONCEPTUALLY_SUCCEEDED, k -> new HashSet<>())
                        .size();
        this.testsTestSuiteError =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.TEST_SUITE_ERROR, k -> new LinkedList<>())
                        .size();
        this.totalTests = context.getTotalTests();
        this.finishedTests = context.getTestsDone();
        this.testCaseCount = context.getTestCases();
        this.scoreContainer = context.getOverallScoreContainer();
        this.configString = context.getConfigString();
        try {
            this.anvilConfigString = new ObjectMapper().writeValueAsString(context.getConfig());
        } catch (JsonProcessingException e) {

        }
        this.endpointType = context.getConfig().getEndpointMode();
        this.running = running;
    }
}
