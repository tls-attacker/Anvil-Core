package de.rub.nds.anvilcore.teststate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilFactoryRegistry;
import de.rub.nds.anvilcore.junit.Utils;
import de.rub.nds.anvilcore.model.ParameterCombination;
import de.rub.nds.anvilcore.teststate.reporting.ScoreContainer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AnvilTestStateContainer {
    private static final Logger LOGGER = LogManager.getLogger();

    // set when the last finished has been reported by junit
    private boolean finished = false;
    // set when coffee4j reports that the input group has been finished
    private boolean readyForCompletion = false;

    private final long startTime = System.currentTimeMillis();
    private int resultRaw = 0;
    private String uniqueId;
    private Method testMethod;
    private Class<?> testClass;

    @JsonProperty("AnnotatedTestResults")
    private List<AnvilTestState> states = new ArrayList<>();

    @JsonProperty("Result")
    private TestResult result;

    @JsonProperty("DisabledReason")
    private String disabledReason;

    @JsonProperty("FailedReason")
    private String failedReason;

    @JsonProperty("ElapsedTime")
    private long elapsedTime = 0;

    @JsonProperty("FailureInducingCombinations")
    private List<ParameterCombination> failureInducingCombinations;

    @JsonUnwrapped private ScoreContainer scoreContainer;

    @Override
    public String toString() {
        return String.format(
                "AnnotatedStateContainer{displayName = %s.%s, result = %s}",
                testClass != null ? testClass.getName() : "undefined",
                testMethod != null ? testMethod.getName() : "undefined",
                result != null ? result.name() : "undefined");
    }

    private AnvilTestStateContainer(ExtensionContext extensionContext) {
        this.uniqueId = extensionContext.getUniqueId();
        this.scoreContainer =
                AnvilFactoryRegistry.get().getScoreContainerFactory().getInstance(extensionContext);
    }

    public static synchronized AnvilTestStateContainer forExtensionContext(
            ExtensionContext extensionContext) {
        ExtensionContext resolvedContext =
                Utils.getTemplateContainerExtensionContext(extensionContext);

        if (AnvilContext.getInstance().getTestResult(resolvedContext.getUniqueId()) != null) {
            return AnvilContext.getInstance().getTestResult(resolvedContext.getUniqueId());
        }

        AnvilTestStateContainer container = new AnvilTestStateContainer(resolvedContext);
        container.setTestClass(resolvedContext.getRequiredTestClass());
        container.setTestMethod(resolvedContext.getTestMethod().orElseThrow());
        AnvilContext.getInstance().addTestResult(container);
        return container;
    }

    public void setResultRaw(int resultRaw) {
        this.resultRaw = resultRaw;
        result = TestResult.resultForBitmask(resultRaw);
        scoreContainer.updateForResult(result);
    }

    public void finish() {
        finished = true;
        result = resolveFinalResult();
        AnvilContext.getInstance().testFinished(uniqueId);
    }

    private TestResult resolveFinalResult() {
        Set<TestResult> uniqueResultTypes =
                states.stream().map(AnvilTestState::getTestResult).collect(Collectors.toSet());
        if (uniqueResultTypes.contains(TestResult.FULLY_FAILED)
                || uniqueResultTypes.contains(TestResult.PARTIALLY_FAILED)) {
            if (uniqueResultTypes.size() > 1) {
                return TestResult.PARTIALLY_FAILED;
            } else {
                return TestResult.FULLY_FAILED;
            }
        }

        if (uniqueResultTypes.contains(TestResult.CONCEPTUALLY_SUCCEEDED)) {
            return TestResult.CONCEPTUALLY_SUCCEEDED;
        }

        if (uniqueResultTypes.contains(TestResult.STRICTLY_SUCCEEDED)
                && uniqueResultTypes.size() == 1) {
            return TestResult.STRICTLY_SUCCEEDED;
        }

        throw new RuntimeException(
                "Failed to determine final result. Unique results obtained: "
                        + uniqueResultTypes.toString());
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public void setTestClass(Class<?> testClass) {
        this.testClass = testClass;
    }

    public boolean isFinished() {
        return finished;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getResultRaw() {
        return resultRaw;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public List<AnvilTestState> getStates() {
        return states;
    }

    public TestResult getResult() {
        return result;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public List<ParameterCombination> getFailureInducingCombinations() {
        return failureInducingCombinations;
    }

    public ScoreContainer getScoreContainer() {
        return scoreContainer;
    }

    public void add(AnvilTestState testState) {
        testState.setAssociatedContainer(this);
        this.states.add(testState);
    }

    public void setFailureInducingCombinations(
            List<ParameterCombination> failureInducingCombinations) {
        this.failureInducingCombinations = failureInducingCombinations;
    }

    /**
     * @return the readyForCompletion
     */
    public boolean isReadyForCompletion() {
        return readyForCompletion;
    }

    /**
     * @param readyForCompletion the readyForCompletion to set
     */
    public void setReadyForCompletion(boolean readyForCompletion) {
        this.readyForCompletion = readyForCompletion;
    }
}
