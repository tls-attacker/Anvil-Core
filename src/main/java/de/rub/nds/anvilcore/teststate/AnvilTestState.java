package de.rub.nds.anvilcore.teststate;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.rub.nds.anvilcore.model.ParameterCombination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public class AnvilTestState {
    private static final Logger LOGGER = LogManager.getLogger();

    @JsonProperty("Result")
    private TestResult testResult;

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("ParameterCombination")
    private ParameterCombination parameterCombination;

    private Throwable failedReason;
    private List<String> additionalResultInformation;
    private List<String> additionalTestInformation;
    private ExtensionContext extensionContext;
    private AnvilTestStateContainer associatedContainer;


    public AnvilTestState() {}

    public AnvilTestState(ParameterCombination parameterCombination, ExtensionContext extensionContext) {
        this.parameterCombination = parameterCombination;
        this.extensionContext = extensionContext;
        this.displayName = extensionContext.getDisplayName();
        this.associatedContainer = AnvilTestStateContainer.forExtensionContext(extensionContext);
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

    public AnvilTestStateContainer getAssociatedContainer() {
        return associatedContainer;
    }

    public void setAssociatedContainer(AnvilTestStateContainer associatedContainer) {
        this.associatedContainer = associatedContainer;
    }
}
