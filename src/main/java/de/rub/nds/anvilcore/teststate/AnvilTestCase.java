/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.rub.nds.anvilcore.model.ParameterCombination;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AnvilTestCase {
    private static final Logger LOGGER = LogManager.getLogger();

    @JsonProperty("Result")
    private TestResult testResult;

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("ParameterCombination")
    private ParameterCombination parameterCombination;

    private Throwable failedReason;

    @JsonProperty("AdditionalResultInformation")
    private List<String> additionalResultInformation;

    @JsonProperty("AdditionalTestInformation")
    private List<String> additionalTestInformation;

    private ExtensionContext extensionContext;
    protected AnvilTestRun associatedContainer;

    public AnvilTestCase() {}

    public AnvilTestCase(
            ParameterCombination parameterCombination, ExtensionContext extensionContext) {
        this.parameterCombination = parameterCombination;
        this.extensionContext = extensionContext;
        this.displayName = extensionContext.getDisplayName();
        this.associatedContainer = AnvilTestRun.forExtensionContext(extensionContext);
        this.associatedContainer.add(this);
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ParameterCombination getParameterCombination() {
        return parameterCombination;
    }

    public void setParameterCombination(ParameterCombination parameterCombination) {
        this.parameterCombination = parameterCombination;
    }

    public Throwable getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(Throwable failedReason) {
        this.failedReason = failedReason;
    }

    public List<String> getAdditionalResultInformation() {
        return additionalResultInformation;
    }

    public void setAdditionalResultInformation(List<String> additionalResultInformation) {
        this.additionalResultInformation = additionalResultInformation;
    }

    public List<String> getAdditionalTestInformation() {
        return additionalTestInformation;
    }

    public void setAdditionalTestInformation(List<String> additionalTestInformation) {
        this.additionalTestInformation = additionalTestInformation;
    }

    public ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    public void setExtensionContext(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    public AnvilTestRun getAssociatedContainer() {
        return associatedContainer;
    }

    public void setAssociatedContainer(AnvilTestRun associatedContainer) {
        this.associatedContainer = associatedContainer;
    }

    public void addAdditionalResultInfo(String info) {
        if (additionalResultInformation == null) {
            additionalResultInformation = new ArrayList<>();
        }

        additionalResultInformation.add(info);
    }

    public void addAdditionalTestInfo(String info) {
        if (additionalTestInformation == null) {
            additionalTestInformation = new ArrayList<>();
        }

        additionalTestInformation.add(info);
    }

    protected void finalizeAnvilTestCase() {}
}
