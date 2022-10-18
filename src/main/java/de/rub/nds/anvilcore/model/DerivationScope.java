package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.annotation.*;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.*;
import org.junit.platform.commons.support.AnnotationSupport;

public class DerivationScope {
    private ModelType modelType = DefaultModelType.ALL_PARAMETERS;
    private final List<ParameterIdentifier> ipmLimitations;
    private final List<ParameterIdentifier> ipmExtensions;
    private final List<ValueConstraint> valueConstraints;
    private final Map<ParameterIdentifier, String> explicitValues;
    private final Map<ParameterIdentifier, String> explicitModelingConstraints;
    private final ExtensionContext extensionContext;
    private final Set<ParameterIdentifier> manualConfigTypes;
    private final int testStrength;

    public DerivationScope(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
        this.ipmLimitations = resolveIpmLimitations(extensionContext);
        this.ipmExtensions = resolveIpmExtensions(extensionContext);
        this.valueConstraints = resolveValueConstraints(extensionContext);
        this.explicitValues = resolveExplicitValues(extensionContext);
        this.explicitModelingConstraints = resolveExplicitModelingConstraints(extensionContext);
        this.manualConfigTypes = resolveManualConfigTypes(extensionContext);
        this.testStrength = resolveTestStrength(extensionContext);
    }

    public DerivationScope(ExtensionContext extensionContext, ModelType modelType) {
        this(extensionContext);
        this.modelType = modelType;
    }

    // TODO Remove constructor, only for testing purposes
    public DerivationScope() {
        this.ipmLimitations = Collections.emptyList();
        this.ipmExtensions = Collections.emptyList();
        this.valueConstraints = Collections.emptyList();
        this.explicitValues = Collections.emptyMap();
        this.explicitModelingConstraints = Collections.emptyMap();
        this.extensionContext = null;
        this.manualConfigTypes = Collections.emptySet();
        this.testStrength = 3;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public List<ParameterIdentifier> getIpmLimitations() {
        return ipmLimitations;
    }

    public List<ParameterIdentifier> getIpmExtensions() {
        return ipmExtensions;
    }

    public List<ValueConstraint> getValueConstraints() {
        return valueConstraints;
    }

    public Map<ParameterIdentifier, String> getExplicitValues() {
        return explicitValues;
    }

    public Map<ParameterIdentifier, String> getExplicitModelingConstraints() {
        return explicitModelingConstraints;
    }

    public ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    public Set<ParameterIdentifier> getManualConfigTypes() {
        return manualConfigTypes;
    }

    public int getTestStrength() {
        return testStrength;
    }

    public boolean hasExplicitValues(ParameterIdentifier parameterIdentifier) {
        return explicitValues.containsKey(parameterIdentifier);
    }

    public boolean hasExplicitModelingConstraints(ParameterIdentifier parameterIdentifier) {
        return explicitModelingConstraints.containsKey(parameterIdentifier);
    }

    /**
     * Return a stream of entries where each entry constists of elements with
     * the same index in {@code firstArray} and {@code secondArray}.
     *
     * @param firstArray elements to use as entry keys
     * @param secondArray elements to use as entry values
     * @return stream of zipped entries from {@code firstArray} and {@code secondArray}
     * @throws ArrayIndexOutOfBoundsException if {@code firstArray} and {@code secondArray} don't have the same length
     */
    private static <A, B> Stream<Map.Entry<A, B>> zipArrays(final A[] firstArray, final B[] secondArray) throws ArrayIndexOutOfBoundsException {
        if(firstArray.length != secondArray.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                "Zipping requires both arrays to have the same size, but first array has %d elements and second array has %d",
                firstArray.length,
                secondArray.length
            ));
        }
        return IntStream
            .range(0, Math.max(firstArray.length, secondArray.length))
            .mapToObj(i -> Map.entry(firstArray[i], secondArray[i]));
    }

    /**
     * Resolve the IPM limitations for the test method in the current context.
     *
     * @param extensionContext the current extension context
     * @return list of parameters that will be removed from the model
     *
     * @see ExcludeParameter
     */
    private static List<ParameterIdentifier> resolveIpmLimitations(final ExtensionContext extensionContext) {
        return Stream.concat(
            AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), IpmLimitations.class)
            .stream()
            .flatMap(annotation -> Arrays.stream(annotation.identifiers())),
            AnnotationSupport.findRepeatableAnnotations(extensionContext.getRequiredTestMethod(), ExcludeParameter.class)
                .stream()
                .map(ExcludeParameter::value))
            .distinct()
            .map(ParameterIdentifier::fromName)
            .collect(Collectors.toList());
    }

    /**
     * Resolve the IPM extensions for the test method in the current context.
     *
     * @param extensionContext the current extension context
     * @return list of parameters that will be added to the model
     *
     * @see IncludeParameter
     */
    private static List<ParameterIdentifier> resolveIpmExtensions(final ExtensionContext extensionContext) {
        return Stream.concat(
            AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), IpmExtensions.class)
                .stream()
                .flatMap(annotation -> Arrays.stream(annotation.identifiers())),
            AnnotationSupport.findRepeatableAnnotations(extensionContext.getRequiredTestMethod(), IncludeParameter.class)
                .stream()
                .map(IncludeParameter::value))
            .distinct()
            .map(ParameterIdentifier::fromName)
            .collect(Collectors.toList());
    }

    /**
     * Resolve the parameter value constraints for the test method in the
     * current context.
     *
     * @param extensionContext the current extension context
     * @return list of parameter value constraints
     *
     * @see ValueConstraints
     * @see DynamicValueConstraints
     */
    private static List<ValueConstraint> resolveValueConstraints(final ExtensionContext extensionContext) {
        return Stream.concat(
            AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), ValueConstraints.class)
                .stream()
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .map(annotation -> new ValueConstraint(
                    ParameterIdentifier.fromName(annotation.identifier()),
                    annotation.method(),
                    annotation.clazz().equals(Object.class) ? extensionContext.getRequiredTestClass() : annotation.clazz(),
                    false
                )),
            AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), DynamicValueConstraints.class)
                .stream()
                .flatMap(annotation -> zipArrays(annotation.affectedIdentifiers(), annotation.methods()))
                .map(entry -> new ValueConstraint(
                    ParameterIdentifier.fromName(entry.getKey()),
                    entry.getValue(),
                    extensionContext.getRequiredTestClass(),
                    true
                ))
            ).collect(Collectors.toList());
    }

    /**
     * Resolve the explicit parameter values for the test method in the current
     * context.
     *
     * @param extensionContext the current extension context
     * @return map of explicit parameter values that should be used
     *
     * @see ExplicitValues
     */
    private static Map<ParameterIdentifier, String> resolveExplicitValues(final ExtensionContext extensionContext) {
        return AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), ExplicitValues.class)
            .stream()
            .flatMap(annotation -> zipArrays(annotation.affectedIdentifiers(), annotation.methods()))
            .map(entry -> Map.entry(ParameterIdentifier.fromName(entry.getKey()), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Resolve the explicit parameter modeling constraints for the test method
     * in the current context.
     *
     * @param extensionContext the current extension context
     * @return map of explicit modeling constraints
     *
     * @see ExplicitModelingConstraints
     */
    private static Map<ParameterIdentifier, String> resolveExplicitModelingConstraints(final ExtensionContext extensionContext) {
        return AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), ExplicitModelingConstraints.class)
            .stream()
            .flatMap(annotation -> zipArrays(annotation.affectedIdentifiers(), annotation.methods()))
            .map(entry -> Map.entry(ParameterIdentifier.fromName(entry.getKey()), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Resolve the set of manually configured parameters for the test method in
     * the current context.
     *
     * @param extensionContext the current extension context
     * @return set of parameter identifiers that are configured manually
     *
     * @see ManualConfig
     */
    private static Set<ParameterIdentifier> resolveManualConfigTypes(final ExtensionContext extensionContext) {
        return AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), ManualConfig.class)
            .stream()
            .flatMap(annotation -> Arrays.stream(annotation.identifiers()))
            .map(ParameterIdentifier::fromName)
            .collect(Collectors.toSet());
    }

    /**
     * Resolve the test strength for the test method in the current context.
     *
     * <p>Returns the default test streng from the {@link AnvilContext} if the
     * method was not found.
     *
     * @param extensionContext the current extension context
     * @return test strength
     *
     * @see TestStrength
     * @see AnvilContext#getTestStrength
     */
    private static int resolveTestStrength(final ExtensionContext extensionContext) {
        return AnnotationSupport.findAnnotation(extensionContext.getRequiredTestMethod(), TestStrength.class)
            .map(TestStrength::value)
            .orElseGet(() -> AnvilContext.getInstance().getTestStrength());
    }
}
