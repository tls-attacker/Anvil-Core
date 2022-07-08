package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static TestContext instance;

    private ApplicationSpecificContextDelegate applicationSpecificContextDelegate;
    private final Map<ParameterType, ParameterFactory> knownParameters = new HashMap<>();
    private int testStrength = 2;

    synchronized public static TestContext getInstance() {
        if (TestContext.instance == null) {
            TestContext.instance = new TestContext();
        }
        return TestContext.instance;
    }

    private TestContext() {}

    public static void setInstance(TestContext instance) {
        TestContext.instance = instance;
    }

    public ApplicationSpecificContextDelegate getApplicationSpecificContextDelegate() {
        return applicationSpecificContextDelegate;
    }

    public void setApplicationSpecificContextDelegate(ApplicationSpecificContextDelegate applicationSpecificContextDelegate) {
        this.applicationSpecificContextDelegate = applicationSpecificContextDelegate;
    }

    public Map<ParameterType, ParameterFactory> getKnownParameters() {
        return knownParameters;
    }

    public ParameterFactory getParameterFactory(ParameterType parameterType) {
        if (!knownParameters.containsKey(parameterType)) {
            throw new IllegalArgumentException("Parameter " + parameterType + " is not known");
        }
        return knownParameters.get(parameterType);
    }

    public int getTestStrength() {
        return testStrength;
    }

    public void setTestStrength(int testStrength) {
        this.testStrength = testStrength;
    }
}
