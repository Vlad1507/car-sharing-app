package com.app.carsharing.validation.match;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FieldMatchValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMatcher {
    String filed();
    String matchedField();
    String message() default "Password fields do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
