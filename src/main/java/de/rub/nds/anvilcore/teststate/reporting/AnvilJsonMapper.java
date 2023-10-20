/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import de.rub.nds.anvilcore.context.AnvilTestConfig;
import de.rub.nds.anvilcore.teststate.AnvilTestRun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilJsonMapper {
    private static final Logger LOGGER = LogManager.getLogger();

    private ObjectMapper mapper;
    private AnvilTestConfig config;

    public AnvilJsonMapper(AnvilTestConfig config) {
        mapper = new ObjectMapper();
        mapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
        mapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        mapper.disable(MapperFeature.AUTO_DETECT_SETTERS);
        mapper.disable(MapperFeature.AUTO_DETECT_CREATORS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());

        this.config = config;
    }

    public void saveTestRunToPath(AnvilTestRun testRun) {
        Path filePath = Paths.get(config.getOutputFolder(), "results", testRun.getTestId());
        filePath = filePath.resolve("_testRun.json");
        File f = new File(filePath.toString());

        try {
            createEmptyFile(filePath.toString());
            mapper.writeValue(f, testRun);
        } catch (Exception e) {
            LOGGER.error("Failed to save AnvilTestRun ({})", testRun.getTestMethodName(), e);
        }
    }

    public void saveReportToPath(AnvilReport report) {
        Path path = Path.of(config.getOutputFolder(), "report.json");
        File f = new File(path.toString());

        try {
            createEmptyFile(path.toString());
            mapper.writeValue(f, report);
        } catch (Exception e) {
            LOGGER.error("Failed to save AnvilReport", e);
        }
    }

    public static void createEmptyFile(String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        f.createNewFile();
    }
}
