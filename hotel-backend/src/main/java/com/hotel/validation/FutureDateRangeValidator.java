package com.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FutureDateRangeValidator implements ConstraintValidator<FutureDateRange, Object> {

    private int maxNights;
    private String checkInField;
    private String checkOutField;

    @Override
    public void initialize(FutureDateRange annotation) {
        this.maxNights     = annotation.maxNights();
        this.checkInField  = annotation.checkInField();
        this.checkOutField = annotation.checkOutField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalDate checkIn  = readDate(value, checkInField);
        LocalDate checkOut = readDate(value, checkOutField);

        if (checkIn == null || checkOut == null) return true;

        if (!checkOut.isAfter(checkIn)) {
            addViolation(context, checkOutField, "Check-out date must be after check-in date");
            return false;
        }
        if (!checkIn.isBefore(checkOut)) {
            return false;
        }
        if (maxNights > 0) {
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights > maxNights) {
                addViolation(context, checkOutField,
                    "Stay may not exceed " + maxNights + " nights (requested: " + nights + ")");
                return false;
            }
        }
        return true;
    }

    private LocalDate readDate(Object obj, String fieldName) {
        try {
            // Support both regular classes and Java records
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) return null;
            field.setAccessible(true);
            Object val = field.get(obj);
            return val instanceof LocalDate ld ? ld : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    private void addViolation(ConstraintValidatorContext ctx, String field, String msg) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(msg)
           .addPropertyNode(field)
           .addConstraintViolation();
    }
}
