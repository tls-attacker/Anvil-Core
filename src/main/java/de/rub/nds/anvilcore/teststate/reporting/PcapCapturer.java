/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate.reporting;

import de.rub.nds.anvilcore.context.AnvilContextRegistry;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

/**
 * Capturer that records <a href="https://en.wikipedia.org/wiki/Pcap">PCAP files</a> while a code
 * block is running.
 */
public class PcapCapturer implements AutoCloseable, Runnable, PacketListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private final PcapHandle pcapHandle;
    private final String tmpFilepath;
    private final PcapDumper pcapDumper;
    private final Thread captureThread;
    private final AnvilTestCase testCase;

    private static final int SNAPSHOT_LENGTH_BYTES = 65_535;
    private static final int READ_TIMEOUT_MILLIS = 50;
    public static final int WAITING_TIME_AFTER_CLOSE_MILLI = 5000;

    /**
     * Create a new Capturer object and start capturing immediately, until {@link #close} is called.
     *
     * @param testCase the AnvilTestCase related to the capturing
     * @throws PcapNativeException if an error occurs in the pcap native library
     * @throws NotOpenException if the pcap handle is not open
     * @throws IOException if an I/O error occurs
     */
    public PcapCapturer(AnvilTestCase testCase)
            throws PcapNativeException, NotOpenException, IOException {

        final PcapNetworkInterface device = getNetworkInterface();
        this.testCase = testCase;
        this.pcapHandle =
                device.openLive(
                        SNAPSHOT_LENGTH_BYTES, PromiscuousMode.NONPROMISCUOUS, READ_TIMEOUT_MILLIS);
        this.tmpFilepath = getTemporaryFilePath();

        this.pcapDumper = this.pcapHandle.dumpOpen(tmpFilepath);
        String filter =
                AnvilContextRegistry.byExtensionContext(testCase.getExtensionContext())
                        .getConfig()
                        .getGeneralPcapFilter();
        if (filter != null && !filter.isEmpty()) {
            this.pcapHandle.setFilter(filter, BpfCompileMode.OPTIMIZE);
        }

        this.captureThread = new Thread(this, "pcap-capture");
        this.captureThread.start();
    }

    private String getTemporaryFilePath() throws IOException {
        String testId = testCase.getAssociatedContainer().getTestId();
        String tmpId = testCase.getTemporaryPcapFileName();

        Path folderPath =
                Paths.get(
                        AnvilContextRegistry.byExtensionContext(testCase.getExtensionContext())
                                .getConfig()
                                .getOutputFolder(),
                        "results",
                        testId);
        Files.createDirectories(folderPath);
        return folderPath.resolve(tmpId).toString();
    }

    private PcapNetworkInterface getNetworkInterface() {
        String interfaceName =
                AnvilContextRegistry.byExtensionContext(testCase.getExtensionContext())
                        .getConfig()
                        .getNetworkInterface();
        if (System.getProperty("os.name").toLowerCase().contains("win")
                && interfaceName.equals("any")) {
            LOGGER.error(
                    "Network-Interface has to be explicitly set on windows. Use the -networkInterface flag.");
            System.exit(1);
        }
        try {
            PcapNetworkInterface networkInterface = Pcaps.getDevByName(interfaceName);
            if (networkInterface == null) {
                StringBuilder builder = new StringBuilder();
                builder.append("Please choose one of the following network interfaces: \n");
                for (PcapNetworkInterface availableInterface : Pcaps.findAllDevs()) {
                    builder.append(" - ").append(availableInterface.getName()).append("\n");
                }
                LOGGER.error("Network interface provided can not be found. " + builder);
                System.exit(1);
            }
            return networkInterface;
        } catch (PcapNativeException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void gotPacket(final Packet packet) {
        try {
            this.pcapDumper.dump(packet, this.pcapHandle.getTimestamp());
        } catch (NotOpenException err) {
            // throw new AnvilException("Failed to dump captured PCAP packet (file not open)", err);
        }
    }

    @Override
    // PMD may complain about the cyclomatic complexity (due to all the `throw`
    // statements), but it's fine to ignore this here.
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void run() {
        // First, loop over available packets. This will wait for more packets
        // if none are available, unless `breakLoop()` has been called.
        try {
            this.pcapHandle.loop(-1, this);
        } catch (PcapNativeException | NotOpenException err) {
            // throw new AnvilException("Failed to capture PCAP packets", err);
            LOGGER.error("Failed to capture PCAP packets");
        } catch (InterruptedException err) {
            LOGGER.debug("breakLoop()` has been called, stop capturing");
        }

        // It's possible that more packets have been queued but not been
        // processed yet. Hence, we call `pcap_dispatch` here, which is similar
        // to `pcap_loop` but does not wait for more packets once the read
        // timeout has been reached.
        //
        // Additionally, we enable non-blocking mode to avoid waiting for
        // packet that haven't been queued altogether.
        try {
            this.pcapHandle.setBlockingMode(PcapHandle.BlockingMode.NONBLOCKING);
        } catch (PcapNativeException | NotOpenException err) {
            LOGGER.error("Failed to set PCAP handle into non-blocking mode");
            throw new RuntimeException();
        }

        try {
            this.pcapHandle.dispatch(-1, this);
        } catch (PcapNativeException | NotOpenException | InterruptedException err) {
            LOGGER.error("Failed to capture queued PCAP packets");
            throw new RuntimeException();
        }

        // At this point all packets have been processed, flush the dump and
        // close the handle.
        try {
            this.pcapDumper.flush();
        } catch (PcapNativeException | NotOpenException err) {
            LOGGER.error("Failed to close PCAP dump (file not open)");
            throw new RuntimeException();
        }
        this.pcapDumper.close();
        this.pcapHandle.close();

        // filter again for specific port and testcase name
        reFilter();
    }

    private void reFilter() {
        // filter the pcap files according to used ports and save them with their uuid
        Path tmpFile = Path.of(tmpFilepath);
        if (!Files.exists(tmpFile)) {
            LOGGER.error("No temporary pcap file found for testcase.");
            return;
        }
        Path finalPcapPath =
                tmpFile.getParent().resolve(String.format("dump_%s.pcap", testCase.getUuid()));
        try (PcapHandle pcapHandle = Pcaps.openOffline(this.tmpFilepath)) {
            if (testCase.getCaseSpecificPcapFilter() != null
                    && !testCase.getCaseSpecificPcapFilter().isEmpty()) {
                pcapHandle.setFilter(testCase.getCaseSpecificPcapFilter(), BpfCompileMode.OPTIMIZE);
            }
            PcapDumper pcapDumper = pcapHandle.dumpOpen(finalPcapPath.toString());
            while (true) {
                try {
                    Packet incomingPacket = pcapHandle.getNextPacketEx();
                    if (incomingPacket != null) {
                        pcapDumper.dump(incomingPacket, pcapHandle.getTimestamp());
                    }
                } catch (EOFException e) {
                    // break on end of file
                    break;
                } catch (TimeoutException e) {
                    LOGGER.error("Error during filtering of pcap files: ", e);
                }
            }
            pcapDumper.close();
        } catch (PcapNativeException | NotOpenException e) {
            LOGGER.error("Error filtering pcap dump: ", e);
        }

        try {
            Files.delete(tmpFile);
        } catch (IOException e) {
            LOGGER.error("Error deleting temporary pcap dump file: ", e);
        }

        if (AnvilContextRegistry.byExtensionContext(testCase.getExtensionContext()).getListener()
                != null) {
            try {
                byte[] pcapBytes = Files.readAllBytes(finalPcapPath);
                AnvilContextRegistry.byExtensionContext(testCase.getExtensionContext())
                        .getListener()
                        .onPcapCaptured(testCase, pcapBytes);
            } catch (IOException e) {
                LOGGER.error("Cannot read pcap file.", e);
            }
        }
    }

    @Override
    public void close() throws PcapNativeException {
        // Break the `pcap_loop` call in the capture thread.
        Executors.newSingleThreadScheduledExecutor()
                .schedule(
                        () -> {
                            try {
                                this.pcapHandle.breakLoop();
                            } catch (NotOpenException err) {
                                LOGGER.error("Failed to break PCAP capture loop (handle not open)");
                                throw new RuntimeException();
                            }
                        },
                        WAITING_TIME_AFTER_CLOSE_MILLI,
                        TimeUnit.MILLISECONDS);
    }
}
