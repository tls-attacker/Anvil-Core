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
import java.util.ArrayList;
import java.util.List;

public class AnvilTestConfig {
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
}
