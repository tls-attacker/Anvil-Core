/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetadataFetcher {

    private static final Logger LOGGER = LogManager.getLogger();

    private Map<String, ?> metadataMap = null;

    public MetadataFetcher() {
        InputStream is = MetadataFetcher.class.getResourceAsStream("/metadata.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.metadataMap = mapper.readValue(is, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> getTestSeverityLevels(String id) {
        if (this.metadataMap.get(id) == null) return null;
        return (Map<String, Integer>) ((Map<?, ?>) this.metadataMap.get(id)).get("severityLevels");
    }

    public Map<?, ?> getRawMetadata(String id) {
        if (!metadataMap.containsKey(id)) {
            LOGGER.error("No metadata found for test with id: " + id);
        }
        return (Map<?, ?>) this.metadataMap.get(id);
    }

    public Set<String> getAllTestIds() {
        return this.metadataMap.keySet();
    }
}
