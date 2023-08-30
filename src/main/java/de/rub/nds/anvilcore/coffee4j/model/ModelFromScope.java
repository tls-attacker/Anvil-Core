package de.rub.nds.anvilcore.coffee4j.model;

import de.rub.nds.anvilcore.model.IpmProvider;
import de.rwth.swc.coffee4j.junit.provider.model.ModelSource;
import java.lang.annotation.*;

/** This is an extended copy of the ModelFromMethod of Coffee4j. */
@Inherited
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ModelSource(IpmProvider.class)
public @interface ModelFromScope {
    String modelType() default "ALL_PARAMETERS";
}
