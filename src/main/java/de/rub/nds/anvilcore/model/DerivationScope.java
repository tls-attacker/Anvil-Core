package de.rub.nds.anvilcore.model;

import de.rub.nds.anvilcore.annotation.*;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.model.constraint.ValueConstraint;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.*;

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


    private static List<ParameterIdentifier> resolveIpmLimitations(ExtensionContext extensionContext) {
        List<ParameterIdentifier> limitations = new ArrayList<>();
        Method testMethod = extensionContext.getRequiredTestMethod();
        if (testMethod.isAnnotationPresent(IpmLimitations.class)) {
            IpmLimitations ipmLimitations = testMethod.getAnnotation(IpmLimitations.class);
            for (String identifier : ipmLimitations.identifiers()) {
                limitations.add(ParameterIdentifier.fromName(identifier));
            }
        }
        return limitations;
    }

    private static List<ParameterIdentifier> resolveIpmExtensions(ExtensionContext extensionContext) {
        List<ParameterIdentifier> extensions = new ArrayList<>();
        Method testMethod = extensionContext.getRequiredTestMethod();
        if (testMethod.isAnnotationPresent(IpmExtensions.class)) {
            IpmExtensions ipmExtensions = testMethod.getAnnotation(IpmExtensions.class);
            for (String identifier : ipmExtensions.identifiers()) {
                extensions.add(ParameterIdentifier.fromName(identifier));
            }
        }
        return extensions;
    }

    private static List<ValueConstraint> resolveValueConstraints(ExtensionContext extensionContext) {
        List<ValueConstraint> constraints = new LinkedList<>();
        Method testMethod = extensionContext.getRequiredTestMethod();
        if (testMethod.isAnnotationPresent(ValueConstraints.class)) {
            ValueConstraints valueConstraints = testMethod.getAnnotation(ValueConstraints.class);
            String[] identifiers = valueConstraints.affectedIdentifiers();
            String[] methods = valueConstraints.methods();
            if(methods.length != identifiers.length) {
                throw new IllegalArgumentException("Unable to resolve ValueConstraints - argument count mismatch");
            }

            for (int i = 0; i < identifiers.length; i++) {
                ParameterIdentifier parameterIdentifier = ParameterIdentifier.fromName(identifiers[i]);
                constraints.add(new ValueConstraint(parameterIdentifier, methods[i], extensionContext.getRequiredTestClass(), false));
            }
        }
        if (testMethod.isAnnotationPresent(DynamicValueConstraints.class)) {
            DynamicValueConstraints valueConstraints = testMethod.getAnnotation(DynamicValueConstraints.class);
            String[] identifiers = valueConstraints.affectedIdentifiers();
            String[] methods = valueConstraints.methods();
            if(methods.length != identifiers.length) {
                throw new IllegalArgumentException("Unable to resolve ValueConstraints - argument count mismatch");
            }

            for (int i = 0; i < identifiers.length; i++) {
                ParameterIdentifier parameterIdentifier = ParameterIdentifier.fromName(identifiers[i]);
                constraints.add(new ValueConstraint(parameterIdentifier, methods[i], extensionContext.getRequiredTestClass(), true));
            }
        }
        return constraints;
    }

    private static Map<ParameterIdentifier, String> resolveExplicitValues(ExtensionContext extensionContext) {
        Map<ParameterIdentifier, String> valueMap = new HashMap<>();
        Method testMethod = extensionContext.getRequiredTestMethod();
        if (testMethod.isAnnotationPresent(ExplicitValues.class)) {
            ExplicitValues explicitValues = testMethod.getAnnotation(ExplicitValues.class);
            String[] identifiers = explicitValues.affectedIdentifiers();
            String[] methods = explicitValues.methods();
            if(methods.length != identifiers.length) {
                throw new IllegalArgumentException("Unable to resolve ExplicitValues - argument count mismatch");
            }
            for (int i = 0; i < identifiers.length; i++) {
                ParameterIdentifier parameterIdentifier = ParameterIdentifier.fromName(identifiers[i]);
                if (valueMap.containsKey(parameterIdentifier)) {
                    throw new IllegalArgumentException("Unable to resolve ExplicitValues - multiple explicit values defined for " + identifiers[i]);
                }
                valueMap.put(parameterIdentifier, methods[i]);
            }
        }
        return valueMap;
    }

    private static Map<ParameterIdentifier, String> resolveExplicitModelingConstraints(ExtensionContext extensionContext) {
        Map<ParameterIdentifier, String> constraintsMap = new HashMap<>();
        Method testMethod = extensionContext.getRequiredTestMethod();
        if(testMethod.isAnnotationPresent(ExplicitModelingConstraints.class)) {
            ExplicitModelingConstraints explicitConstraints = testMethod.getAnnotation(ExplicitModelingConstraints.class);
            String[] identifiers = explicitConstraints.affectedIdentifiers();
            String[] methods = explicitConstraints.methods();
            if(methods.length != identifiers.length) {
                throw new IllegalArgumentException("Unable to resolve ExplicitModelParameterConstraints - argument count mismatch");
            }
            for (int i = 0; i < identifiers.length; i++) {
                ParameterIdentifier parameterIdentifier = ParameterIdentifier.fromName(identifiers[i]);
                if (constraintsMap.containsKey(parameterIdentifier)) {
                    throw new IllegalArgumentException("Unable to resolve ExplicitModelParameterConstraints - multiple explicit constraints defined for " + identifiers[i]);
                }
                constraintsMap.put(parameterIdentifier, methods[i]);
            }
        }
        return constraintsMap;
    }

    private static Set<ParameterIdentifier> resolveManualConfigTypes(ExtensionContext extensionContext) {
        Set<ParameterIdentifier> manualConfigTypes = new HashSet<>();
        Method testMethod = extensionContext.getRequiredTestMethod();
        if(testMethod.isAnnotationPresent(ManualConfig.class)) {
            ManualConfig manualConfig = testMethod.getAnnotation(ManualConfig.class);
            for (String identifier : manualConfig.identifiers()) {
                manualConfigTypes.add(ParameterIdentifier.fromName(identifier));
            }
        }
        return manualConfigTypes;
    }

    private static int resolveTestStrength(ExtensionContext extensionContext) {
        Method testMethod = extensionContext.getRequiredTestMethod();
        if(testMethod.isAnnotationPresent(TestStrength.class)) {
            TestStrength testStrength = testMethod.getAnnotation(TestStrength.class);
            return testStrength.value();
        }
        return AnvilContext.getInstance().getTestStrength();
    }
}
