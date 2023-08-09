package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.teststate.TestResult;
import java.util.Date;
import java.util.LinkedList;

public class AnvilReport {
    @JsonProperty("ElapsedTime")
    private long elapsedTime;

    @JsonProperty("Identifier")
    private String identifier;

    @JsonProperty("Date")
    private Date date;

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

    @JsonProperty("AdditionalConfig")
    private String configString;

    @JsonProperty("AnvilConfig")
    private String anvilConfigString;

    @JsonUnwrapped private ScoreContainer scoreContainer;

    public AnvilReport(AnvilContext context) {
        this.elapsedTime = System.currentTimeMillis() - context.getCreationTime().getTime();
        this.identifier = context.getConfig().getIdentifier();
        this.date = context.getCreationTime();
        this.testsDisabled =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.DISABLED, k -> new LinkedList<>())
                        .size();
        this.testsFullyFailed =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.FULLY_FAILED, k -> new LinkedList<>())
                        .size();
        this.testsStrictlySucceeded =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.STRICTLY_SUCCEEDED, k -> new LinkedList<>())
                        .size();
        this.testsPartiallyFailed =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.PARTIALLY_FAILED, k -> new LinkedList<>())
                        .size();
        this.testsConceptuallySucceeded =
                context.getResultTestMap()
                        .computeIfAbsent(TestResult.CONCEPTUALLY_SUCCEEDED, k -> new LinkedList<>())
                        .size();
        this.scoreContainer = context.getScoreContainer();
        this.configString = context.getConfigString();
        try {
            this.anvilConfigString = new ObjectMapper().writeValueAsString(context.getConfig());
        } catch (JsonProcessingException e) {

        }
    }
}
