package de.rub.nds.anvilcore.execution;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.junit.reporting.AnvilTestExecutionListener;
import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * The TestRunner is used to start the testing phase. It is the entrypoint for anvil implementations
 * that defined their own tests. The {@link AnvilTestConfig} supplies the TestRunner with
 * information regarding the test strength and parallel tests. A {@link AnvilListener} can be used
 * as a callback before or after test execution.
 */
public class TestRunner {
    private static final Logger LOGGER = LogManager.getLogger();

    private final AnvilTestConfig config;
    private final AnvilContext context;

    public TestRunner(
            AnvilTestConfig config, String configString, ParameterIdentifierProvider provider) {
        this.config = config;
        this.context = AnvilContext.createInstance(config, configString, provider);

        this.config.restrictParallelization();
    }

    public void setListener(AnvilListener listener) {
        context.setListener(listener);
    }

    public void runTests(String packageName) {
        config.setTestPackage(packageName);
        runTests();
    }

    /**
     * Starts the testpahse. This function will search for JUnit tests with the {@link
     * de.rub.nds.anvilcore.annotation.AnvilTest} tag and execute them with varying parameters.
     * Tests are discovered based on the testPackage parameter in the AnvilConfig.
     */
    public void runTests() {

        LauncherDiscoveryRequestBuilder builder =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(selectPackage(config.getTestPackage()))
                        // https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.mode.default", "same_thread")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.mode.classes.default",
                                "concurrent")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.config.strategy", "fixed")
                        .configurationParameter(
                                "junit.jupiter.execution.parallel.config.fixed.parallelism",
                                String.valueOf(config.getParallelTests()));

        if (!config.getTags().isEmpty()) {
            builder.filters(TagFilter.includeTags(config.getTags()));
        }

        LauncherDiscoveryRequest request = builder.build();

        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        AnvilTestExecutionListener anvilTestListener = new AnvilTestExecutionListener();

        Launcher launcher =
                LauncherFactory.create(
                        LauncherConfig.builder()
                                .enableTestExecutionListenerAutoRegistration(false)
                                .addTestExecutionListeners(summaryListener)
                                .addTestExecutionListeners(anvilTestListener)
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
        context.setTotalTests(testcases);

        if (context.getListener() != null) {
            context.getListener().beforeStart(testplan);
        }

        long start = System.currentTimeMillis();
        launcher.execute(request);

        double elapsedTime = (System.currentTimeMillis() - start) / 1000.0;
        if (elapsedTime < 10) {
            LOGGER.error("Something seems to be wrong, testsuite executed in " + elapsedTime + "s");
        }

        TestExecutionSummary summary = summaryListener.getSummary();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos, true);
        summary.printTo(writer);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        LOGGER.info("\n" + content);
    }
}