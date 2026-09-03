package com.aurum.main.annotations;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private String defaultRegion;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        this.defaultRegion = constraintAnnotation.defaultRegion();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext validator) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(value, defaultRegion);

            return phoneUtil.isValidNumber(number);
        } catch (Exception e) {
            return false;
        }
    }
}
