/*
 * Anvil Core - A combinatorial testing framework for cryptographic protocols based on coffee4j
 *
 * Copyright 2022-2023 Ruhr University Bochum, Paderborn University, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.anvilcore.coffee4j.junit;

import de.rub.nds.anvilcore.junit.extension.AnvilTestWatcher;
import de.rwth.swc.coffee4j.engine.report.ReportLevel;
import de.rwth.swc.coffee4j.junit.provider.configuration.reporter.ReporterSource;
import de.rwth.swc.coffee4j.model.report.ExecutionReporter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a {@link ReporterSource} using the {@link ConstructorBasedReporterProvider} to create new
 * instances of a {@link ExecutionReporter} via a no-args, or one-arg constructor. Since reporters
 * are allowed, just return multiple classes in the {@link #value()} method to register more
 * reporters, or use any other {@link ReporterSource} since {@link ReporterSource} is a repeatable
 * annotation.
 *
 * <p>If {@link #useLevel()} is set to {@code false} (this is the default) a no-args constructor is
 * used to instantiate the {@link ExecutionReporter}s. Otherwise, the {@link ReportLevel} defined by
 * {@link #level()} is given to the constructor so an excepting constructor is needed. This can be
 * used to directly configure the level to which reporters should listen at the method.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ReporterSource(ConstructorBasedReporterProvider.class)
public @interface AnvilReporter {

    /**
     * @return the class of {@link ExecutionReporter} which listen to combinatorial test execution.
     *     Either a no-args or constructor needing one {@link ReportLevel} is needed depending on
     *     the setting of {@link #useLevel()} as described in {@link AnvilReporter}
     */
    Class<? extends AnvilTestWatcher>[] value();

    /**
     * @return at which level the {@link ExecutionReporter}s should listen if {@link #useLevel()} is
     *     enabled
     */
    ReportLevel level() default ReportLevel.INFO;

    /**
     * @return whether to use {@link ReportLevel} during the instantiation of all {@link
     *     ExecutionReporter}s
     */
    boolean useLevel() default false;
}
