package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import de.rub.nds.anvilcore.model.parameter.ParameterFactory;
import de.rub.nds.anvilcore.model.parameter.ParameterType;
import de.rub.nds.anvilcore.teststate.reporting.DefaultScoreContainerFactory;
import de.rub.nds.anvilcore.teststate.reporting.ScoreContainerFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilFactoryRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static AnvilFactoryRegistry instance;

    private ParameterIdentifierProvider parameterIdentifierProvider;
    private ScoreContainerFactory scoreContainerFactory = new DefaultScoreContainerFactory();
    private final Map<ParameterType, ParameterFactory> knownParameters = new HashMap<>();

    private AnvilFactoryRegistry() {}

    public static AnvilFactoryRegistry get() {
        if (instance == null) {
            instance = new AnvilFactoryRegistry();
        }
        return instance;
    }

    public ParameterIdentifierProvider getParameterIdentifierProvider() {
        if (parameterIdentifierProvider == null) {
            throw new RuntimeException("No ParameterIdentifierProvider registered");
        }
        return parameterIdentifierProvider;
    }

    public void setParameterIdentifierProvider(
            ParameterIdentifierProvider parameterIdentifierProvider) {
        this.parameterIdentifierProvider = parameterIdentifierProvider;
    }

    public ScoreContainerFactory getScoreContainerFactory() {
        return scoreContainerFactory;
    }

    public void setScoreContainerFactory(ScoreContainerFactory scoreContainerFactory) {
        this.scoreContainerFactory = scoreContainerFactory;
    }

    public Map<ParameterType, ParameterFactory> getKnownParameters() {
        return knownParameters;
    }

    public void addParameterTypes(
            ParameterType[] parameterTypes, ParameterFactory associatedFactory) {
        for (ParameterType parameterType : parameterTypes) {
            if (knownParameters.containsKey(parameterType)) {
                LOGGER.warn("Parameter type " + parameterType.toString() + " already exists");
            }
            knownParameters.put(parameterType, associatedFactory);
        }
    }

    public ParameterFactory getParameterFactory(ParameterType parameterType) {
        if (!knownParameters.containsKey(parameterType)) {
            throw new IllegalArgumentException("Parameter " + parameterType + " is not known");
        }
        return knownParameters.get(parameterType);
    }
}
