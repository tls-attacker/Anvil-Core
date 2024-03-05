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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilReport {
    private static final Logger LOGGER = LogManager.getLogger();

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

    @JsonProperty("DetailsFailedTestCases")
    private Map<String, Integer> detailsFailedTestCases;

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
                context.getResultsTestRuns()
                        .computeIfAbsent(TestResult.DISABLED, k -> new LinkedList<>())
                        .size();
        this.testsFullyFailed =
                context.getResultsTestRuns()
                        .computeIfAbsent(TestResult.FULLY_FAILED, k -> new LinkedList<>())
                        .size();
        this.testsStrictlySucceeded =
                context.getResultsTestRuns()
                        .computeIfAbsent(TestResult.STRICTLY_SUCCEEDED, k -> new LinkedList<>())
                        .size();
        this.testsPartiallyFailed =
                context.getResultsTestRuns()
                        .computeIfAbsent(TestResult.PARTIALLY_FAILED, k -> new LinkedList<>())
                        .size();
        this.testsConceptuallySucceeded =
                context.getResultsTestRuns()
                        .computeIfAbsent(TestResult.CONCEPTUALLY_SUCCEEDED, k -> new LinkedList<>())
                        .size();
        this.testsTestSuiteError =
                context.getResultsTestRuns()
                        .computeIfAbsent(TestResult.TEST_SUITE_ERROR, k -> new LinkedList<>())
                        .size();
        this.detailsFailedTestCases =
                context.getDetailsFailedTestCases().values().stream()
                        .flatMap(List::stream)
                        .collect(
                                Collectors.groupingBy(
                                        String::toString, Collectors.summingInt(detail -> 1)));
        this.totalTests = context.getTotalTestRuns();
        this.finishedTests = context.getTestRunsDone();
        this.testCaseCount = context.getTestCases();
        this.scoreContainer = context.getOverallScoreContainer();
        this.configString = context.getConfigString();
        try {
            this.anvilConfigString = new ObjectMapper().writeValueAsString(context.getConfig());
        } catch (JsonProcessingException e) {
            LOGGER.error("Error occurred while processing anvil test config to JSON string. ", e);
        }
        this.endpointType = context.getConfig().getEndpointMode();
        this.running = running;
    }
}
