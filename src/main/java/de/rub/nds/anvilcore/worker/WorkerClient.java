/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.worker;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.execution.AnvilListener;
import de.rub.nds.anvilcore.execution.TestRunner;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import de.rub.nds.anvilcore.teststate.reporting.AnvilReport;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.platform.launcher.TestPlan;

public class WorkerClient implements AnvilListener {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String API_URL = "/api/v2/";
    private static final int POLLING_TIME = 10;

    private enum WorkerStatus {
        IDLE,
        WORKING,
        PAUSED
    }

    private final String hostname;
    private final String testPackage;
    private final ParameterIdentifierProvider parameterIdentifierProvider;
    private final HttpClient client;
    private final ObjectMapper mapper;

    private String workerId;
    private boolean alive;
    private WorkerStatus status;
    private Map<String, Map<?, ?>> jobs;
    private CompletableFuture<Void> currentTestRun = null;
    private String activeJobId = null;
    private ExecutorService pool;

    private AnvilListener listener;

    public WorkerClient(String hostname, String packageName, ParameterIdentifierProvider provider) {
        this.hostname = hostname;
        this.testPackage = packageName;
        this.parameterIdentifierProvider = provider;
        this.client =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(3))
                        .build();
        this.mapper = new ObjectMapper();
        mapper.setVisibility(
                mapper.getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());
        this.alive = false;
        this.status = WorkerStatus.IDLE;
        this.jobs = new LinkedHashMap<>();
    }

    public AnvilListener getListener() {
        return listener;
    }

    public void setListener(AnvilListener listener) {
        this.listener = listener;
    }

    public void run() throws InterruptedException {
        LOGGER.info("WorkerClient starting");
        int MAX_TRIES = 3;
        for (int i = 0; i < MAX_TRIES; i++) {
            if (registerWorker()) break;
            LOGGER.info("Trying again in 5 seconds");
            Thread.sleep(5000);
        }
        if (!this.alive) {
            LOGGER.info("Could not connect to backend. Exiting.");
            return;
        }
        LOGGER.info("Connected");
        while (this.alive) {
            fetch();
            Thread.sleep(POLLING_TIME * 1000);
        }
    }

    private boolean registerWorker() {
        LOGGER.info("Connecting to backend...");
        Map<String, String> body = new LinkedHashMap<>();

        body.put("name", "testWorker");
        try {
            Map<?, ?> registerResponse = postRequest("worker/register", body);
            this.workerId = (String) registerResponse.get("id");
            this.alive = true;
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error connecting to backend: " + e.getMessage());
            return false;
        }
    }

    private void fetch() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("id", this.workerId);
        body.put("status", this.status.toString());
        try {
            Map<?, ?> command = postRequest("worker/fetch", body);
            queueCommand(command);
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Connection to backend lost: " + e.getMessage());
            this.alive = false;
        }
    }

    public void queueCommand(Map<?, ?> commandObject) {
        String command = (String) commandObject.get("command");
        switch (command) {
            case "QUEUE_RUN":
                {
                    Map<?, ?> job = (Map<?, ?>) commandObject.get("job");
                    queueJob(job);
                    break;
                }
            case "STOP_RUN":
                {
                    String jobId = (String) commandObject.get("jobId");
                    stopRun(jobId, false);
                    break;
                }
            case "RESTART":
                {
                    String jobId = (String) commandObject.get("jobId");
                    stopRun(jobId, true);
                    break;
                }
            case "RESTART_RUN":
                {
                    String jobId = (String) commandObject.get("jobId");
                    String className = (String) commandObject.get("className");
                    String methodName = (String) commandObject.get("methodName");
                    restartAnvilRun(jobId, className, methodName);
                    break;
                }
            case "RESTART_CASE":
                {
                    String jobId = (String) commandObject.get("jobId");
                    String className = (String) commandObject.get("className");
                    String methodName = (String) commandObject.get("methodName");
                    String uuid = (String) commandObject.get("uuid");
                    restartCase(jobId, className, methodName, uuid);
                    break;
                }
            case "OK":
                break;
            default:
                LOGGER.error("Unknown command received from controller.");
        }
    }

    private void queueJob(Map<?, ?> job) {
        // if we are still working, put job to queue, else we can start the run
        if (this.status == WorkerStatus.WORKING) {
            jobs.put((String) job.get("id"), job);
        } else {
            startTestRun(job);
        }
    }

    private void startTestRun(Map<?, ?> job) {
        String anvilConfigString = (String) job.get("config");
        String additionalConfigString = (String) job.get("additionalConfig");
        AnvilTestConfig anvilConfig = new AnvilTestConfig();
        try {
            ObjectMapper configMapper = new ObjectMapper();
            configMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            anvilConfig = configMapper.readValue(anvilConfigString, AnvilTestConfig.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not read AnvilTestConfig", e);
        }

        if (listener != null) {
            listener.gotConfig(anvilConfig, additionalConfigString);
        }

        activeJobId = (String) job.get("id");
        status = WorkerStatus.WORKING;
        jobs.put(activeJobId, job);
        LOGGER.info("Starting new test execution.");
        if (this.pool == null || this.pool.isTerminated()) {
            this.pool = Executors.newFixedThreadPool(1);
        }
        AnvilTestConfig finalAnvilConfig = anvilConfig;
        currentTestRun =
                CompletableFuture.runAsync(
                                () -> {
                                    TestRunner runner =
                                            new TestRunner(
                                                    finalAnvilConfig,
                                                    additionalConfigString,
                                                    parameterIdentifierProvider);
                                    runner.setListener(this);
                                    runner.runTests();
                                })
                        .thenRun(
                                () -> {
                                    jobs.remove(activeJobId);
                                    Optional<Map<?, ?>> nextJob =
                                            jobs.values().stream().findFirst();
                                    if (nextJob.isPresent()) {
                                        startTestRun(nextJob.get());
                                    } else {
                                        status = WorkerStatus.IDLE;
                                    }
                                });
    }

    private void stopRun(String jobId, boolean restart) {
        // cancel current run if jobId matches
        if (jobId.equals(activeJobId)) {
            LOGGER.info("Canceling current test execution.");
            if (listener != null) {
                listener.onAborted();
            }
            AnvilContext.getInstance().abortRemainingTests();
            pool.shutdown();
            currentTestRun.cancel(true);
            CompletableFuture.runAsync(
                    () -> {
                        // send last report
                        AnvilReport report = new AnvilReport(AnvilContext.getInstance(), false);
                        postTestReportUpdate(report, true);
                        // next job if available
                        Optional<Map<?, ?>> nextJob;
                        if (restart) { // if we want to restart, keep current job
                            nextJob = Optional.of(jobs.get(activeJobId));
                        } else {
                            nextJob = jobs.values().stream().findFirst();
                        }
                        if (nextJob.isPresent()) {
                            startTestRun(nextJob.get());
                        } else {
                            status = WorkerStatus.IDLE;
                        }
                    },
                    CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS));
        }
        if (!restart) {
            // remove job from list
            Map<?, ?> job = jobs.get(jobId);
            if (job != null) {
                jobs.remove(jobId);
            }
        }
    }

    private void restartAnvilRun(String jobId, String methodName, String className) {}

    private void restartCase(String jobId, String methodName, String className, String uuid) {
        // todo reimplement
    }

    private void startCase(
            String jobId,
            String methodName,
            String className,
            ParameterCombination parameterCombination) {
        // todo reimplement
    }

    private void restartTestrun(String methodName, String className) {
        // todo reimplement
    }

    private void startTestrun(String methodName, String className) {
        // todo reimplement
    }

    @Override
    public void onTestCaseFinished(AnvilTestCase testCase, String className, String methodName) {
        if (listener != null) {
            listener.onTestCaseFinished(testCase, className, methodName);
        }
        Map<String, Object> stateUpdate = new LinkedHashMap<>();
        stateUpdate.put("jobId", activeJobId);
        stateUpdate.put("className", className);
        stateUpdate.put("methodName", methodName);
        stateUpdate.put("testCase", testCase);

        postUpdateAsync("worker/update/testcase", stateUpdate);
    }

    @Override
    public void onTestRunFinished(AnvilTestRun testRun) {
        if (listener != null) {
            listener.onTestRunFinished(testRun);
        }
        Map<String, Object> testUpdate = new LinkedHashMap<>();
        testUpdate.put("jobId", activeJobId);
        testUpdate.put("testRun", testRun);

        postUpdateAsync("worker/update/testrun", testUpdate);
    }

    @Override
    public void onReportFinished(AnvilReport anvilReport) {
        if (listener != null) {
            listener.onReportFinished(anvilReport);
        }
        postTestReportUpdate(anvilReport, true);
    }

    public void postTestReportUpdate(AnvilReport anvilReport, boolean finished) {
        Map<String, Object> reportUpdate = new LinkedHashMap<>();
        reportUpdate.put("jobId", activeJobId);
        reportUpdate.put("report", anvilReport);
        reportUpdate.put("finished", finished);

        postUpdateAsync("worker/update/report", reportUpdate);
    }

    @Override
    public void onStarted() {
        if (listener != null) {
            listener.onStarted();
        }
        Map<String, Object> statusUpdate = new LinkedHashMap<>();
        statusUpdate.put("jobId", activeJobId);
        statusUpdate.put("status", "TESTING");

        postUpdateAsync("worker/update/status", statusUpdate);
    }

    @Override
    public void onAborted() {
        if (listener != null) {
            listener.onAborted();
        }
    }

    @Override
    public boolean beforeStart(TestPlan testPlan, long totalTests) {

        Map<String, Object> statusUpdate = new LinkedHashMap<>();
        statusUpdate.put("jobId", activeJobId);
        statusUpdate.put("status", "SCANNING");
        statusUpdate.put("totalTests", totalTests);

        postUpdateAsync("worker/update/status", statusUpdate);

        if (listener != null) {
            return listener.beforeStart(testPlan, totalTests);
        } else {
            return true;
        }
    }

    private Map<?, ?> getRequest(String location) throws IOException, InterruptedException {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("http://" + this.hostname + API_URL + location))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();
        HttpResponse<String> response =
                this.client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        return mapper.readValue(body, Map.class);
    }

    private Map<?, ?> postRequest(String location, Map<?, ?> object)
            throws IOException, InterruptedException {
        // System.out.println("Sending: " + mapper.writeValueAsString(object));
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("http://" + this.hostname + API_URL + location))
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        mapper.writeValueAsString(object)))
                        .build();
        HttpResponse<String> response =
                this.client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        System.out.println("Receiving: " + body);
        return mapper.readValue(body, Map.class);
    }

    private void postUpdateAsync(String location, Map<?, ?> object) {
        CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return postRequest(location, object);
                            } catch (Exception e) {
                                throw new CompletionException(e);
                            }
                        })
                .whenComplete(
                        (response, exception) -> {
                            if (exception != null) {
                                LOGGER.error(
                                        "Error posting update to backend: " + exception.toString());
                            } else {
                                LOGGER.info("Posted update to backend.");
                            }
                        });
    }
}
