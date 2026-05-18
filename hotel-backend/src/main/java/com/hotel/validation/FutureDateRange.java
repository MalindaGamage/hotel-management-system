package com.hotel.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a date range (checkInDate / checkOutDate fields) satisfies:
 * - checkInDate is today or later
 * - checkOutDate is strictly after checkInDate
 * - stay duration does not exceed maxNights
 *
 * Apply at the class level on any record/DTO that has checkInDate and checkOutDate fields.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureDateRangeValidator.class)
@Documented
public @interface FutureDateRange {
    String message() default "Check-out must be after check-in and the stay may not exceed {maxNights} nights";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /** Maximum allowed stay length in nights (0 = no limit). */
    int maxNights() default 30;

    /** Field name for the check-in date (must be LocalDate). */
    String checkInField() default "checkInDate";

    /** Field name for the check-out date (must be LocalDate). */
    String checkOutField() default "checkOutDate";
}
