package com.app.carsharing.validation.match;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchValidator implements ConstraintValidator<FieldMatcher, Object> {
    private String field;
    private String matchedField;

    @Override
    public void initialize(FieldMatcher constraintAnnotation) {
        this.field = constraintAnnotation.filed();
        this.matchedField = constraintAnnotation.matchedField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object field = new BeanWrapperImpl(value).getPropertyValue(this.field);
        Object matchedField = new BeanWrapperImpl(value).getPropertyValue(this.matchedField);
        return Objects.equals(field, matchedField);
    }
}
