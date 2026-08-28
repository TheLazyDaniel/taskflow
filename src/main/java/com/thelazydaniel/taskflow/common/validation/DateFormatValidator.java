package com.thelazydaniel.taskflow.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {

    private String pattern;

    @Override
    public void initialize(ValidDateFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        try {
            LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (DateTimeParseException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Invalid date format. Expected format: %s", pattern)
            ).addConstraintViolation();
            return false;
        }
    }
}