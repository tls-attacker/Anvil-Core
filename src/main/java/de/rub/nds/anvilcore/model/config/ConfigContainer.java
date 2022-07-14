package de.rub.nds.anvilcore.model.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigContainer {
    private final Map<Class<? extends AnvilConfig>, AnvilConfig> configs = new HashMap<>();

    public AnvilConfig getConfig(Class<? extends AnvilConfig> configClass) {
        if (!configs.containsKey(configClass)) {
            throw new RuntimeException("Requested config does not exist");
        }
        return configs.get(configClass);
    }

    public void addConfig(Class<? extends AnvilConfig> configClass, AnvilConfig config) {
        if (!config.getClass().equals(configClass)) {
            throw new IllegalArgumentException("configClass does not match class of passed config");
        }
        if (configs.containsKey(configClass)) {
            throw new IllegalArgumentException("A config of that class already exists");
        }
        configs.put(configClass, config);
    }

    public static ConfigContainer fromConfig(Class<? extends AnvilConfig> configClass, AnvilConfig config) {
        ConfigContainer configContainer = new ConfigContainer();
        configContainer.addConfig(configClass, config);
        return configContainer;
    }
}
