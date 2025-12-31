package com.photon.identity.onboarding.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileFormatTypeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileFormatsForType {
    String message() default "Invalid supportedFormats for given type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
