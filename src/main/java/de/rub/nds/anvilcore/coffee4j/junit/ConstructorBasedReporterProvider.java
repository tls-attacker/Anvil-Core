/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.coffee4j.junit;

import de.rwth.swc.coffee4j.engine.report.ReportLevel;
import de.rwth.swc.coffee4j.junit.provider.configuration.reporter.ReporterProvider;
import de.rwth.swc.coffee4j.model.report.ExecutionReporter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;

public class ConstructorBasedReporterProvider
        implements ReporterProvider, AnnotationConsumer<AnvilReporter> {

    private Class<? extends ExecutionReporter>[] reporterClasses;

    private ReportLevel level;
    private ExtensionContext extensionContext;

    @Override
    public void accept(AnvilReporter reporter) {
        reporterClasses = reporter.value();
        level = reporter.useLevel() ? reporter.level() : null;
    }

    @Override
    public List<ExecutionReporter> provide(ExtensionContext extensionContext) {
        final List<ExecutionReporter> reporters = new ArrayList<>();
        this.extensionContext = extensionContext;

        for (Class<? extends ExecutionReporter> reporterClass : reporterClasses) {
            reporters.add(
                    level == null
                            ? instantiateReporter(reporterClass)
                            : instantiateReporterWithLevel(reporterClass));
        }

        return reporters;
    }

    private ExecutionReporter instantiateReporter(
            Class<? extends ExecutionReporter> reporterClass) {
        try {
            return reporterClass
                    .getConstructor(ExtensionContext.class)
                    .newInstance(extensionContext);
        } catch (Exception e) {
            final String message =
                    "Could not create a new instance of "
                            + reporterClass.getSimpleName()
                            + " with a default constructor";
            throw new JUnitException(message, e);
        }
    }

    private ExecutionReporter instantiateReporterWithLevel(
            Class<? extends ExecutionReporter> reporterClass) {
        final Constructor<? extends ExecutionReporter> constructor =
                getRequiredConstructor(reporterClass);

        try {
            return constructor.newInstance(extensionContext, level);
        } catch (Exception e) {
            final String message =
                    "Could not create a new instance of the given constructor " + constructor;
            throw new JUnitException(message, e);
        }
    }

    private Constructor<? extends ExecutionReporter> getRequiredConstructor(
            Class<? extends ExecutionReporter> reporterClass) {
        try {
            return reporterClass.getConstructor(ExtensionContext.class, ReportLevel.class);
        } catch (NoSuchMethodException e) {
            final String message =
                    "The class "
                            + reporterClass.getName()
                            + " must have a constructor which accepts a "
                            + ReportLevel.class.getSimpleName();
            throw new JUnitException(message, e);
        }
    }
}
