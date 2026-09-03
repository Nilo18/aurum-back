package com.aurum.main.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class) // Links annotation to the engine below
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Invalid international phone number";
    String defaultRegion() default "GE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
