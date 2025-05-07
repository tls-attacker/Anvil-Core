/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.context;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rub.nds.anvilcore.constants.TestEndpointType;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilTestConfig {
    static Logger LOGGER = LogManager.getLogger();

    @Parameter(names = "-expectedResults", description = "Path to expectedResults as json")
    private String expectedResults = null;

    @Parameter(names = "-profiles", description = "Which profiles should be used")
    private List<String> profiles = new ArrayList<String>();

    @Parameter(
            names = "-profileFolder",
            description = "Path to the Folder which contains the profile definitions as json files")
    private String profileFolder = System.getProperty("user.dir");

    @Parameter(names = "-tags", description = "Run only tests containing on of the specified tags")
    private List<String> tags = new ArrayList<>();

    @Parameter(
            names = "-testPackage",
            description = "Run only tests included in the specified package")
    private String testPackage = null;

    @Parameter(
            names = "-ignoreCache",
            description = "Do not use cached information (e.g for feature extraction)")
    private boolean ignoreCache = false;

    @Parameter(
            names = "-outputFolder",
            description =
                    "Folder where the test results should be stored inside, defaults to `pwd/Results_$(date)`")
    private String outputFolder = "";

    @Parameter(
            names = "-parallelTestCases",
            description = "How many test cases should be executed in parallel?")
    private int parallelTestCases = 5;

    @Parameter(
            names = "-parallelTests",
            description =
                    "How many test templates should be executed in parallel? (Default value: parallelHandshakes * 1.5)")
    private Integer parallelTests = null;

    @Parameter(
            names = "-restartTargetAfter",
            description =
                    "How many test cases should be executed before restarting the target? (Default value: 0 = infinite)")
    private Integer restartServerAfter = 0;

    @Parameter(
            names = "-timeoutActionScript",
            description =
                    "Script to execute, if the execution of the test suite "
                            + "seems to make no progress",
            variableArity = true)
    private List<String> timeoutActionCommand = new ArrayList<>();

    @Parameter(
            names = "-identifier",
            description = "Identifier that is visible in the serialized test result. ")
    private String identifier = null;

    @Parameter(
            names = "-strength",
            description = "Strength of the pairwise test. (Default value: 2)")
    private int strength = 2;

    @Parameter(
            names = "-connectionTimeout",
            description = "The default timeout to use for communication with the target")
    private int connectionTimeout = 1500;

    @Parameter(names = "-prettyPrintJSON", description = "Pretty print json output")
    private boolean prettyPrintJSON = false;

    @Parameter(
            names = "-networkInterface",
            description =
                    "Network interface from which packets are recorded using tcpdump. "
                            + "(Default value: any")
    private String networkInterface = "any";

    @Parameter(
            names = "-disableTcpDump",
            description = "Disables the packet capturing with tcpdump")
    private boolean disableTcpDump = false;

    @Parameter(names = "-zip", description = "Pack the results folder into a zip archive.")
    private boolean doZip = false;

    private TestEndpointType endpointMode;
    private String generalPcapFilter = "";

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public String getProfileFolder() {
        return profileFolder;
    }

    public void setProfileFolder(String profileFolder) {
        this.profileFolder = profileFolder;
    }

    public TestEndpointType getEndpointMode() {
        return endpointMode;
    }

    public void setEndpointMode(TestEndpointType endpointMode) {
        this.endpointMode = endpointMode;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTestPackage() {
        return testPackage;
    }

    public void setTestPackage(String testPackage) {
        this.testPackage = testPackage;
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    public void setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public int getParallelTestCases() {
        return parallelTestCases;
    }

    public void setParallelTestCases(int parallelTestCases) {
        this.parallelTestCases = parallelTestCases;
    }

    public Integer getParallelTests() {
        return parallelTests;
    }

    public void setParallelTests(Integer parallelTests) {
        this.parallelTests = parallelTests;
    }

    public Integer getRestartServerAfter() {
        return restartServerAfter;
    }

    public void setRestartServerAfter(Integer restartServerAfter) {
        this.restartServerAfter = restartServerAfter;
    }

    public List<String> getTimeoutActionCommand() {
        return timeoutActionCommand;
    }

    public void setTimeoutActionCommand(List<String> timeoutActionCommand) {
        this.timeoutActionCommand = timeoutActionCommand;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public boolean isPrettyPrintJSON() {
        return prettyPrintJSON;
    }

    public void setPrettyPrintJSON(boolean prettyPrintJSON) {
        this.prettyPrintJSON = prettyPrintJSON;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public boolean isDisableTcpDump() {
        return disableTcpDump;
    }

    public void setDisableTcpDump(boolean disableTcpDump) {
        this.disableTcpDump = disableTcpDump;
    }

    public void restrictParallelization() {
        parallelTestCases = Math.min(parallelTestCases, Runtime.getRuntime().availableProcessors());
        if (parallelTests == null) {
            parallelTests = (int) Math.ceil(parallelTestCases * 1.5);
        }
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing config to string. ", e);
            throw new RuntimeException(e);
        }
    }

    public static AnvilTestConfig fromString(String configString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(configString, AnvilTestConfig.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error deserializing string to config. ", e);
            throw new RuntimeException(e);
        }
    }

    public String getGeneralPcapFilter() {
        return generalPcapFilter;
    }

    public void setGeneralPcapFilter(String generalPcapFilter) {
        this.generalPcapFilter = generalPcapFilter;
    }

    public boolean isDoZip() {
        return doZip;
    }

    public void setDoZip(boolean doZip) {
        this.doZip = doZip;
    }
}
