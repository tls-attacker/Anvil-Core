/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.context;

import de.rub.nds.anvilcore.model.ParameterIdentifierProvider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.TestPlan;

/**
 * Registry for managing multiple AnvilContext instances identified by unique IDs. This replaces the
 * singleton pattern in AnvilContext to support concurrent test execution with different
 * configurations.
 */
public class AnvilContextRegistry {
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);
    private static final ConcurrentHashMap<String, AnvilContext> CONTEXTS =
            new ConcurrentHashMap<>();
    private static final String CONTEXT_ID_PARAMETER = "anvil.context.id";

    /**
     * Creates a new AnvilContext instance with a unique ID.
     *
     * @param config the test configuration
     * @param configString the configuration string
     * @param provider the parameter identifier provider
     * @return the unique ID for the created context
     */
    public static String createContext(
            AnvilTestConfig config, String configString, ParameterIdentifierProvider provider) {
        String contextId = "anvil-context-" + ID_COUNTER.incrementAndGet();
        AnvilContext context = new AnvilContext(config, configString, provider);
        CONTEXTS.put(contextId, context);
        return contextId;
    }

    /**
     * Retrieves an AnvilContext by its ID.
     *
     * @param contextId the context ID
     * @return the AnvilContext instance, or null if not found
     */
    public static AnvilContext getContext(String contextId) {
        return CONTEXTS.get(contextId);
    }

    /**
     * Removes an AnvilContext from the registry.
     *
     * @param contextId the context ID to remove
     * @return the removed AnvilContext, or null if not found
     */
    public static AnvilContext removeContext(String contextId) {
        return CONTEXTS.remove(contextId);
    }

    /** Clears all contexts from the registry. */
    public static void clearAll() {
        CONTEXTS.clear();
    }

    /**
     * Returns the number of active contexts.
     *
     * @return the number of contexts in the registry
     */
    public static int getActiveContextCount() {
        return CONTEXTS.size();
    }

    /**
     * Retrieves an AnvilContext by extracting the context ID from the JUnit TestPlan. This is a
     * convenience method for JUnit TestExecutionListener methods that need to access their specific
     * context.
     *
     * @param testPlan the JUnit TestPlan
     * @return the AnvilContext instance for the context ID found in the test plan, or null if not
     *     found
     */
    public static AnvilContext byTestPlan(TestPlan testPlan) {
        String contextId =
                testPlan.getConfigurationParameters().get(CONTEXT_ID_PARAMETER).orElse(null);
        if (contextId == null) {
            return null;
        }
        return getContext(contextId);
    }

    /**
     * Retrieves an AnvilContext by extracting the context ID from the JUnit ExtensionContext. This
     * is a convenience method for JUnit tests that need to access their specific context.
     *
     * @param extensionContext the JUnit ExtensionContext
     * @return the AnvilContext instance for the context ID found in the extension context, or null
     *     if not found
     */
    public static AnvilContext byExtensionContext(ExtensionContext extensionContext) {
        String contextId = getContextIdFromExtensionContext(extensionContext);
        if (contextId == null) {
            return null;
        }
        return getContext(contextId);
    }

    /**
     * Extracts the context ID from the JUnit ExtensionContext configuration parameters.
     *
     * @param extensionContext the JUnit ExtensionContext
     * @return the context ID, or null if not found
     */
    private static String getContextIdFromExtensionContext(ExtensionContext extensionContext) {
        ExtensionContext current = extensionContext;
        while (current != null) {
            String contextId = current.getConfigurationParameter(CONTEXT_ID_PARAMETER).orElse(null);
            if (contextId != null) {
                return contextId;
            }
            current = current.getParent().orElse(null);
        }
        return null;
    }
}
