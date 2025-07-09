/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.execution;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import de.rub.nds.anvilcore.annotation.AnvilTest;
import de.rub.nds.anvilcore.annotation.NonCombinatorialAnvilTest;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilContextRegistry;
import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.context.ProfileResolver;
import de.rub.nds.anvilcore.junit.extension.AnvilTestWatcher;
import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import de.rub.nds.anvilcore.teststate.TestResult;
import de.rub.nds.anvilcore.teststate.reporting.PcapCapturer;
import java.lang.reflect.Method;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * The TestRunner is used to start the testing phase. It is the entrypoint for anvil implementations
 * that defined their own tests. The {@link AnvilTestConfig} supplies the TestRunner with
 * information regarding the test strength and parallel tests. A {@link AnvilListener} can be used
 * as a callback before or after test execution.
 */
public class TestRunner {
    public static final String CONTEXT_ID_PROPERTY = "anvil.context.id";
    private static final Logger LOGGER = LogManager.getLogger();

    private final AnvilTestConfig config;
    private final AnvilContext context;
    private final String contextId;
    private boolean checkExecuted = false;
    private boolean checkPassed = false;

    public TestRunner(
            AnvilTestConfig config, String configString, ParameterIdentifierProvider provider) {
        this.config = config;
        this.contextId = AnvilContextRegistry.createContext(config, configString, provider);
        this.context = AnvilContextRegistry.getContext(contextId);
    }

    public void setListener(AnvilListener listener) {
        context.setListener(listener);
    }

    public void runTests(String packageName) {
        config.setTestPackage(packageName);
        runTests();
    }

    /**
     * Starts the testpahse. This function will search for JUnit tests with the {@link AnvilTest}
     * tag and execute them with varying parameters. Tests are discovered based on the testPackage
     * parameter in the AnvilConfig.
     */
    public void runTests() {

        if (config.getExpectedResults() != null) {
            config.parseExpectedResults();
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")
                && config.getNetworkInterface().equals("any")) {
            LOGGER.warn(
                    "To capture traffic on windows, the network interface has to be explicitly set. "
                            + "Use the -networkInterface flag. Disabling network traffic capture for now.");
            config.setDisableTcpDump(true);
        }

        if (config.getParallelTests() == null) {
            config.setParallelTests(1);
        }

        LauncherDiscoveryRequestBuilder builder =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(selectPackage(config.getTestPackage()))
                        // https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution
                        .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.mode.default", "same_thread")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.mode.classes.default",
                                "concurrent")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.config.strategy", "fixed")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.config.fixed.parallelism",
                                String.valueOf(config.getParallelTests()))
                        .configurationParameter(CONTEXT_ID_PROPERTY, contextId);

        if (!config.getTags().isEmpty()) {
            builder.filters(TagFilter.includeTags(config.getTags()));
        }
        if (!config.getProfiles().isEmpty()) {
            restrictToProfile(builder);
        }
        restrictToAnvilTests(builder);
        LauncherDiscoveryRequest request = builder.build();
        AnvilTestWatcher anvilTestWatcher = new AnvilTestWatcher(contextId);

        Launcher launcher =
                LauncherFactory.create(
                        LauncherConfig.builder()
                                .enableTestExecutionListenerAutoRegistration(false)
                                .addTestExecutionListeners(anvilTestWatcher)
                                .build());

        TestPlan testplan = launcher.discover(request);
        long testcases =
                testplan.countTestIdentifiers(
                        i -> {
                            TestSource source = i.getSource().orElse(null);
                            return i.isTest()
                                    || (source != null
                                            && source.getClass().equals(MethodSource.class));
                        });
        context.setTotalTestRuns(testcases);

        if (context.getListener() != null) {
            context.getListener().beforeStart(testplan, testcases);
        }

        long start = System.currentTimeMillis();
        launcher.execute(request);

        double elapsedTime = (System.currentTimeMillis() - start) / 1000.0;
        if (elapsedTime < 10) {
            LOGGER.error("Something seems to be wrong, testsuite executed in " + elapsedTime + "s");
        }

        // wait for all pcap files to be written
        if (!config.isDisableTcpDump()) {
            LOGGER.info("Stopping pcap capture...");
            try {
                Thread.sleep(PcapCapturer.WAITING_TIME_AFTER_CLOSE_MILLI);
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
        }
        if (config.getExpectedResultsMap() != null) {
            checkExpectedResults();
        }
    }

    private void checkExpectedResults() {

        Map<TestResult, Set<String>> results =
                AnvilContextRegistry.getContext(contextId).getResultsTestRuns();
        Map<String, TestResult> orderedActualResults = new HashMap<>();
        Map<String, TestResult> orderedExpectedResults = new HashMap<>();
        for (Map.Entry<TestResult, Set<String>> entry : results.entrySet()) {
            for (String s : entry.getValue()) {
                orderedActualResults.put(s, entry.getKey());
            }
        }
        for (Map.Entry<TestResult, Set<String>> entry : config.getExpectedResultsMap().entrySet()) {
            for (String s : entry.getValue()) {
                orderedExpectedResults.put(s, entry.getKey());
            }
        }

        boolean fail = false;
        StringBuilder builder = new StringBuilder();
        builder.append("Comparing to expected results:\n");
        builder.append(String.format(" %-20s| %-20s| %-20s\n", "Actual", "Expected", "Test"));
        builder.append("---------------------+---------------------+---------------------\n");
        Set<String> unionSet = new HashSet<>();
        unionSet.addAll(orderedExpectedResults.keySet());
        unionSet.addAll(orderedActualResults.keySet());
        for (String testId : unionSet) {
            if (!orderedActualResults.containsKey(testId)) {
                builder.append(
                        String.format(
                                " %-20s| %-20s| %-20s\n",
                                "missing", orderedExpectedResults.get(testId), testId));
                fail = true;
            } else if (!orderedExpectedResults.containsKey(testId)) {
                if (orderedActualResults.get(testId) != TestResult.STRICTLY_SUCCEEDED
                        && orderedActualResults.get(testId) != TestResult.CONCEPTUALLY_SUCCEEDED
                        && orderedActualResults.get(testId) != TestResult.DISABLED) {
                    builder.append(
                            String.format(
                                    " %-20s| %-20s| %-20s\n",
                                    orderedActualResults.get(testId), "not present", testId));
                    fail = true;
                }
            } else if (orderedActualResults.get(testId) != orderedExpectedResults.get(testId)) {
                builder.append(
                        String.format(
                                " %-20s| %-20s| %-20s\n",
                                orderedActualResults.get(testId),
                                orderedExpectedResults.get(testId),
                                testId));
                fail = true;
            }
        }
        if (fail) {
            LOGGER.error(builder);
        } else {
            LOGGER.info("Results match expected results.");
        }
        checkExecuted = true;
        checkPassed = !fail;
    }

    private void restrictToProfile(LauncherDiscoveryRequestBuilder builder) {
        ProfileResolver profileResolver = new ProfileResolver(config.getProfileFolder());
        List<String> ids = profileResolver.resolve(config.getProfiles());
        builder.filters(
                (PostDiscoveryFilter)
                        descriptor -> {
                            if (descriptor instanceof MethodBasedTestDescriptor md) {
                                Method method = md.getTestMethod();
                                String anvilTestId = null;
                                if (method.isAnnotationPresent(AnvilTest.class)) {
                                    anvilTestId = method.getAnnotation(AnvilTest.class).id();
                                } else if (method.isAnnotationPresent(
                                        NonCombinatorialAnvilTest.class)) {
                                    anvilTestId =
                                            method.getAnnotation(NonCombinatorialAnvilTest.class)
                                                    .id();
                                } else {
                                    LOGGER.warn("Method {} has no ID", method);
                                }
                                if (anvilTestId != null) {
                                    if (ids.contains(anvilTestId)) {
                                        return FilterResult.included("Profile includes ID");
                                    } else {
                                        return FilterResult.excluded("Profile does not include ID");
                                    }
                                }
                                return FilterResult.excluded("Method has no ID");
                            }
                            return FilterResult.included(
                                    "Method is not instance of MethodBasedTestDescriptor");
                        });
    }

    private void restrictToAnvilTests(LauncherDiscoveryRequestBuilder builder) {
        builder.filters(
                (PostDiscoveryFilter)
                        descriptor -> {
                            if (descriptor instanceof MethodBasedTestDescriptor md) {
                                Method method = md.getTestMethod();
                                if (method.isAnnotationPresent(AnvilTest.class)
                                        || method.isAnnotationPresent(
                                                NonCombinatorialAnvilTest.class)) {
                                    return FilterResult.included("");
                                } else {
                                    return FilterResult.excluded("Method is not an AnvilTest");
                                }
                            } else if (descriptor instanceof ClassBasedTestDescriptor cd) {
                                if (cd.getTestClass().isAnnotationPresent(AnvilTest.class)
                                        || cd.getTestClass()
                                                .isAnnotationPresent(
                                                        NonCombinatorialAnvilTest.class)) {
                                    return FilterResult.included("");
                                } else {
                                    return FilterResult.excluded("Class is not an AnvilTest");
                                }
                            }
                            return FilterResult.excluded("Test is not an AnvilTest");
                        });
    }

    public boolean isCheckExecuted() {
        return checkExecuted;
    }

    public boolean isCheckPassed() {
        return checkPassed;
    }

    public String getContextId() {
        return contextId;
    }
}
