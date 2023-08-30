package de.rub.nds.anvilcore.teststate.reporting;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
        mapper.setVisibility(
                mapper.getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());

        this.config = config;
    }

    public void saveTestRunToPath(AnvilTestRun testRun) {
        String method = testRun.getTestMethodName();
        // truncate the class name to shorten the path length
        // basically throw away the common package, i.e. everything before "server" or "client"
        String pName = config.getTestPackage();
        method = method.replace(pName + ".", "");

        String[] folderComponents = method.split("\\.");

        Path filePath = Paths.get(config.getOutputFolder(), folderComponents);
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
