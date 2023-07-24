package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.Date;

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

    @JsonUnwrapped private ScoreContainer scoreContainer;

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTestsStrictlySucceeded() {
        return testsStrictlySucceeded;
    }

    public void setTestsStrictlySucceeded(long testsStrictlySucceeded) {
        this.testsStrictlySucceeded = testsStrictlySucceeded;
    }

    public long getTestsDisabled() {
        return testsDisabled;
    }

    public void setTestsDisabled(long testsDisabled) {
        this.testsDisabled = testsDisabled;
    }

    public long getTestsFullyFailed() {
        return testsFullyFailed;
    }

    public void setTestsFullyFailed(long testsFullyFailed) {
        this.testsFullyFailed = testsFullyFailed;
    }

    public ScoreContainer getScoreContainer() {
        return scoreContainer;
    }

    public void setScoreContainer(ScoreContainer scoreContainer) {
        this.scoreContainer = scoreContainer;
    }

    public long getTestsConceptuallySucceeded() {
        return testsConceptuallySucceeded;
    }

    public void setTestsConceptuallySucceeded(long testsConceptuallySucceeded) {
        this.testsConceptuallySucceeded = testsConceptuallySucceeded;
    }

    public long getTestsPartiallyFailed() {
        return testsPartiallyFailed;
    }

    public void setTestsPartiallyFailed(long testsPartiallyFailed) {
        this.testsPartiallyFailed = testsPartiallyFailed;
    }
}
