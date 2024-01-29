/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.execution;

import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.anvilcore.teststate.reporting.PcapCapturer;
import java.io.IOException;
import java.lang.reflect.Method;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

public class PcapCapturingInvocationInterceptor implements InvocationInterceptor {

    private static final Logger LOGGER = LogManager.getLogger();

    public void interceptTestTemplateMethod(
            final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext)
            throws Throwable {

        if (AnvilContext.getInstance().getConfig().isDisableTcpDump()) {
            invocation.proceed();
            return;
        }

        AnvilTestCase testCase = AnvilTestCase.fromExtensionContext(extensionContext);

        // start capturing - auto closes when test is done
        try (PcapCapturer pcapCapturer = new PcapCapturer(testCase)) {
            invocation.proceed();
        } catch (PcapNativeException | NotOpenException | IOException ex) {
            LOGGER.error("Failed to start packet capture: {}", ex.getLocalizedMessage());
            // continue invocation even if pcap can not be recorded
            invocation.proceed();
        }
    }
}
