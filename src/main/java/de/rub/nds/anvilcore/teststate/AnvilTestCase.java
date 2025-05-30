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
import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

    @JsonProperty("SrcPort")
    private Integer srcPort = null;

    @JsonProperty("DstPort")
    private Integer dstPort = null;

    private Date startTime;
    private Date endTime;

    private ExtensionContext extensionContext;
    protected AnvilTestRun associatedContainer;

    private String caseSpecificPcapFilter = null;
    private String temporaryPcapFileName = null;
    private static int pcapFileCounter = 0;

    public AnvilTestCase() {}

    public AnvilTestCase(
            ParameterCombination parameterCombination, ExtensionContext extensionContext) {
        this.parameterCombination = parameterCombination;
        this.extensionContext = extensionContext;
        this.displayName = extensionContext.getDisplayName();
        this.associatedContainer = AnvilTestRun.forExtensionContext(extensionContext);
        this.associatedContainer.addTestCase(this);
        this.testResult = TestResult.NOT_SPECIFIED;
    }

    public static AnvilTestCase fromExtensionContext(ExtensionContext extensionContext) {
        return (AnvilTestCase)
                extensionContext
                        .getStore(ExtensionContext.Namespace.GLOBAL)
                        .get(AnvilTestCase.class.getName());
    }

    @JsonProperty("uuid")
    public String getUuid() {
        StringBuilder toHash = new StringBuilder();
        toHash.append(this.getAdditionalTestInformation());
        if (getParameterCombination() != null) {
            toHash.append(this.getParameterCombination().toString());
        }
        toHash.append(getAssociatedContainer().getTestClass().getName());
        toHash.append(getAssociatedContainer().getTestMethod().getName());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(toHash.toString().getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not possible...");
        }
    }

    @JsonProperty("Stacktrace")
    public String getStacktrace() {
        if (failedReason != null) {
            if (failedReason instanceof AssertionError) {
                return failedReason.toString();
            } else {
                return ExceptionUtils.getStackTrace(failedReason);
            }
        }
        return null;
    }

    @JsonProperty("StartTimestamp")
    public String getStartTimestamp() {
        if (startTime == null) return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(startTime);
    }

    @JsonProperty("EndTimestamp")
    public String getEndTimestamp() {
        if (endTime == null) return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(endTime);
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

    /**
     * Generates a string containing the failure reason and additional result information of a
     * failed test case. This string is used for logging purposes.
     *
     * @return The generated string
     */
    public String getFailureDetails() {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(
                String.format(
                        "failure reason: %s.",
                        getFailedReason() != null ? getFailedReason().getMessage() : "undefined"));
        if (getAdditionalResultInformation() != null) {
            logMessage.append(
                    String.format(
                            " Additional result info: %s.",
                            getAdditionalResultInformation().toString()));
        }

        return logMessage.toString();
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

    public String getCaseSpecificPcapFilter() {
        return caseSpecificPcapFilter;
    }

    public void setCaseSpecificPcapFilter(String caseSpecificPcapFilter) {
        this.caseSpecificPcapFilter = caseSpecificPcapFilter;
    }

    public String getTemporaryPcapFileName() {
        if (temporaryPcapFileName == null) {
            temporaryPcapFileName = String.format("testcase_%d.pcap", pcapFileCounter);
            pcapFileCounter++;
        }
        return temporaryPcapFileName;
    }

    public void setTemporaryPcapFileName(String temporaryPcapFileName) {
        this.temporaryPcapFileName = temporaryPcapFileName;
    }

    public Integer getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(Integer srcPort) {
        this.srcPort = srcPort;
    }

    public Integer getDstPort() {
        return dstPort;
    }

    public void setDstPort(Integer dstPort) {
        this.dstPort = dstPort;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
