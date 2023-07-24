package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Date;

public class AnvilReport {
    @JsonProperty("ElapsedTime")
    private long elapsedTime;
    @JsonProperty("Identifier")
    private String identifier;
    @JsonProperty("NumberStates")
    private long numStates;
    @JsonProperty("Date")
    private Date date;
    @JsonProperty("SucceededTests")
    private long testsSucceeded;
    @JsonProperty("DisabledTests")
    private long testsDisabled;
    @JsonProperty("FailedTests")
    private long testsFailed;
    @JsonUnwrapped
    private ScoreContainer scoreContainer;


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

    public long getNumStates() {
        return numStates;
    }

    public void setNumStates(long numStates) {
        this.numStates = numStates;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTestsSucceeded() {
        return testsSucceeded;
    }

    public void setTestsSucceeded(long testsSucceeded) {
        this.testsSucceeded = testsSucceeded;
    }

    public long getTestsDisabled() {
        return testsDisabled;
    }

    public void setTestsDisabled(long testsDisabled) {
        this.testsDisabled = testsDisabled;
    }

    public long getTestsFailed() {
        return testsFailed;
    }

    public void setTestsFailed(long testsFailed) {
        this.testsFailed = testsFailed;
    }

    public ScoreContainer getScoreContainer() {
        return scoreContainer;
    }

    public void setScoreContainer(ScoreContainer scoreContainer) {
        this.scoreContainer = scoreContainer;
    }
}
