package de.rub.nds.anvilcore.junit.extension;

import de.rub.nds.anvilcore.context.AnvilContext;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestrunAbortedCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if (AnvilContext.getInstance().isAborted()) {
            return ConditionEvaluationResult.disabled("Testrun is aborted");
        } else {
            return ConditionEvaluationResult.enabled("Testrun is active.");
        }
    }
}
