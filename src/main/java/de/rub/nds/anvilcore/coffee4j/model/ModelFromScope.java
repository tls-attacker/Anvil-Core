package de.rub.nds.anvilcore.coffee4j.model;

import de.rwth.swc.coffee4j.junit.provider.model.ModelSource;

import java.lang.annotation.*;

/**
 * 
 *  This is an extended copy of the ModelFromMethod of Coffee4j.
 */
@Inherited
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ModelSource(ScopeBasedProvider.class)
public @interface ModelFromScope {
    String name() default "AnvilTest";
    String modelType() default "all_parameters";
}
