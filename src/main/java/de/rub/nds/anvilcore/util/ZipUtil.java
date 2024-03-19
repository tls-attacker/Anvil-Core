/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZipUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void packFolderToZip(String sourceDir, String zipFilename) {
        try {
            Path sourceDirPath = Paths.get(sourceDir);
            Path zipFilePath = Files.createFile(sourceDirPath.resolve(zipFilename));
            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFilePath));
                    Stream<Path> paths = Files.walk(sourceDirPath)) {
                paths.filter(path -> !Files.isDirectory(path))
                        .filter(path -> !path.equals(zipFilePath))
                        .forEach(
                                path -> {
                                    ZipEntry zipEntry =
                                            new ZipEntry(sourceDirPath.relativize(path).toString());
                                    try {
                                        zs.putNextEntry(zipEntry);
                                        Files.copy(path, zs);
                                        zs.closeEntry();
                                    } catch (IOException e) {
                                        LOGGER.error("Error writing to zip file: ", e);
                                    }
                                });
            }
        } catch (IOException e) {
            LOGGER.error("Error writing zip file: ", e);
        }
    }
}
