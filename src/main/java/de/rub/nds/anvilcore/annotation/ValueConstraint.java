package de.rub.nds.anvilcore.annotation;

import java.lang.annotation.*;

@Repeatable(ValueConstraints.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ValueConstraint {
    String identifier();

    String method();
}
