/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.teststate.reporting;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    /** PCAP handle that reads from the network interface. */
    private final PcapHandle pcapHandle;

    /** PCAP dumper instance that represents the output file. */
    private final PcapDumper pcapDumper;

    /** The thread which takes care of the actual capturing. */
    private final Thread captureThread;

    private AnvilTestCase testCase;

    /** Builder for a PcapCapturer instance. */
    public static class Builder {
        PcapDumper dumper;

        /**
         * File path of the PCAP file to write to.
         *
         * <p>Setting this to a non-empty value is required.
         */
        private Optional<String> filePath = Optional.empty();

        /**
         * Name of the network interface to capture on.
         *
         * <p>On non-windows platforms this value is set {@code "any"} by default.
         */
        private String interfaceName =
                (System.getProperty("os.name").toLowerCase().indexOf("win") == -1)
                        ? "any"
                        : AnvilContext.getInstance().getConfig().getNetworkInterface();
        /**
         * Whether to use promiscuous mode for the network interface or not.
         *
         * <p>This value is set to non-promiscuous mode by default.
         */
        private PromiscuousMode promiscuousMode = PromiscuousMode.NONPROMISCUOUS;

        /**
         * Optional filter expression for the Berkeley Packet Filter (BPF).
         *
         * <p>This value is empty by default.
         */
        private Optional<String> bpfExpression = Optional.empty();

        /**
         * The maximum length of a captured packet.
         *
         * <p>According to the {@code pcap(3)} man page:
         *
         * <p>
         *
         * <blockquote>
         *
         * A snapshot length of 65535 should be sufficient, on most if not all networks, to capture
         * all the data available from the packet.
         *
         * </blockquote>
         *
         * <p>Hence, this value is set to 65535 by default.
         */
        private int snapshotLengthBytes = 65_535;

        /**
         * Time to wait for next packet.
         *
         * <p>This value is set to 50 by default.
         */
        private int readTimeoutMillis = 50;

        private AnvilTestCase testCase;

        /**
         * Set the file path of the PCAP file to write to.
         *
         * @param filePath path of the PCAP file
         * @return builder instance
         */
        public Builder withFilePath(final String filePath) {
            this.filePath = Optional.of(filePath);
            return this;
        }

        /**
         * Set the network interface to capture on.
         *
         * @param interfaceName name of the network interface
         * @return builder instance
         */
        public Builder withInterfaceName(final String interfaceName) {
            this.interfaceName = Objects.requireNonNull(interfaceName);
            return this;
        }

        /**
         * Set the promiscuous mode of the interface.
         *
         * @param promiscuousMode promiscuous mode setting
         * @return builder instance
         */
        public Builder withPromiscuousMode(final PromiscuousMode promiscuousMode) {
            this.promiscuousMode = promiscuousMode;
            return this;
        }

        /**
         * Set the BPF expression.
         *
         * @param bpfExpression filter expression
         * @return builder instance
         */
        public Builder withBpfExpression(final String bpfExpression) {
            this.bpfExpression = Optional.of(bpfExpression);
            return this;
        }

        /**
         * Set the snapshot length.
         *
         * @param snapshotLengthBytes snapshot length in bytes
         * @return builder instance
         */
        public Builder withSnapshotLengthBytes(final int snapshotLengthBytes) {
            this.snapshotLengthBytes = snapshotLengthBytes;
            return this;
        }

        /**
         * Set the read timeout.
         *
         * @param readTimeoutMillis read timeout in milliseconds
         * @return builder instance
         */
        public Builder withReadTimeoutMillis(final int readTimeoutMillis) {
            this.readTimeoutMillis = readTimeoutMillis;
            return this;
        }

        public Builder withTestCase(AnvilTestCase testCase) {
            this.testCase = testCase;
            return this;
        }

        /**
         * Build a {@link PcapCapturer} and start capturing.
         *
         * @return active pcap capturer
         * @throws PcapNativeException if a native PCAP error occurred
         * @throws NotOpenException if a file or device failed to open
         */
        public PcapCapturer build() throws PcapNativeException, NotOpenException {
            return new PcapCapturer(
                    this.filePath.orElseThrow(
                            () ->
                                    new NoSuchElementException(
                                            "A file path is required to capturing a PCAP file")),
                    this.interfaceName,
                    this.promiscuousMode,
                    this.bpfExpression,
                    this.snapshotLengthBytes,
                    this.readTimeoutMillis,
                    this.testCase);
        }
    }

    /**
     * Create a builder to configure a {@link PcapCapturer}.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new Capturer object and start capturing immediately, until {@link #close} is called.
     *
     * @param filePath path of the PCAP file to write to
     * @param interfaceName name of the network interface to capture on
     * @param bpfExpression optional filter expression for the Berkeley Packet Filter (BPF)
     * @param snapshotLengthBytes maximum length of a captured packet.
     */
    protected PcapCapturer(
            final String filePath,
            final String interfaceName,
            final PromiscuousMode promiscuousMode,
            final Optional<String> bpfExpression,
            final int snapshotLengthBytes,
            final int readTimeoutMillis,
            AnvilTestCase testCase)
            throws PcapNativeException, NotOpenException {
        final PcapNetworkInterface device = Pcaps.getDevByName(interfaceName);
        this.pcapHandle = device.openLive(snapshotLengthBytes, promiscuousMode, readTimeoutMillis);
        this.pcapDumper = this.pcapHandle.dumpOpen(filePath);
        if (bpfExpression.isPresent()) {
            this.pcapHandle.setFilter(bpfExpression.get(), BpfCompileMode.OPTIMIZE);
        }

        this.testCase = testCase;

        this.captureThread = new Thread(this, "pcap-capture");
        this.captureThread.start();
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

        this.testCase.finalizeAnvilTestCase();
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
                        5,
                        TimeUnit.SECONDS);
    }
}
