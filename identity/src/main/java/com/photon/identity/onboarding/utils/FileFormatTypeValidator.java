package com.photon.identity.onboarding.utils;

import com.photon.identity.onboarding.dto.File;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileFormatTypeValidator implements ConstraintValidator<ValidFileFormatsForType, File> {

    @Override
    public boolean isValid(File dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getType() == null || dto.getSupportedFormats() == null) {
            return true;
        }

        boolean allMatch = dto.getSupportedFormats()
                .stream()
                .allMatch(format -> format.getFileType() == dto.getType());

        if (!allMatch) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("All supportedFormats must match FileType '%s'", dto.getType())
            ).addConstraintViolation();
        }

        return allMatch;
    }
}