package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.model.DefaultModelType;
import de.rub.nds.anvilcore.model.ModelBasedIpmFactory;
import de.rub.nds.anvilcore.model.ModelType;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AnvilContext {
    private static final Logger LOGGER = LogManager.getLogger();

    private static AnvilContext instance;

    private ApplicationSpecificContextDelegate applicationSpecificContextDelegate;
    private int testStrength = 2;

    private final Map<ParameterType, ParameterFactory> knownParameters = new HashMap<>();
    private final List<ModelType> knownModelTypes;
    private ModelBasedIpmFactory modelBasedIpmFactory;

    private final Map<String, Boolean> finishedTests = new HashMap<>();

    synchronized public static AnvilContext getInstance() {
        if (AnvilContext.instance == null) {
            AnvilContext.instance = new AnvilContext();
        }
        return AnvilContext.instance;
    }

    private AnvilContext() {
        knownModelTypes = new ArrayList<>();
        knownModelTypes.addAll(Arrays.asList(DefaultModelType.values()));
    }

    public static void setInstance(AnvilContext instance) {
        AnvilContext.instance = instance;
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

    public List<ModelType> getKnownModelTypes() {
        return knownModelTypes;
    }

    public ModelBasedIpmFactory getModelBasedIpmFactory() {
        return modelBasedIpmFactory;
    }

    public void setModelBasedIpmFactory(ModelBasedIpmFactory modelBasedIpmFactory) {
        this.modelBasedIpmFactory = modelBasedIpmFactory;
    }

    public Map<String, Boolean> getFinishedTests() {
        return finishedTests;
    }

    public int getTestStrength() {
        return testStrength;
    }

    public void setTestStrength(int testStrength) {
        this.testStrength = testStrength;
    }

    public boolean testIsFinished(String uniqueId) {
        return finishedTests.containsKey(uniqueId);
    }
}
