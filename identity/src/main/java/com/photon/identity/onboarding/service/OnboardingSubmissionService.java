package com.photon.identity.onboarding.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.onboarding.cache.FormValidationCacheService;
import com.photon.identity.onboarding.dto.OnboardingSubmissionRequestDto;
import com.photon.identity.onboarding.entity.OnboardingFormSubmission;
import com.photon.identity.onboarding.repository.OnboardingFormSubmissionRepository;
import com.photon.identity.authentication.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingSubmissionService {

    private final UserService userService;
    private final FormValidationCacheService formValidationCacheService;
    private final OnboardingFormSubmissionRepository repository;
    private final ObjectMapper objectMapper;

    public ApiResponseDto<?> saveStep(OnboardingSubmissionRequestDto dto) {
        try {
            OnboardingFormSubmission submission = repository
                .findBySessionIdAndFormId(dto.getSessionId(), dto.getFormId())
                .orElse(OnboardingFormSubmission.builder().sessionId(dto.getSessionId()).scenarioId(dto.getScenarioId())
                        .formId(dto.getFormId()).step(dto.getStep()).build());

            submission.setFormData(objectMapper.writeValueAsString(dto.getFormData()));
            submission.setSubmissionState(dto.getSubmissionState());
            submission.setFinalStep(dto.isFinalStep());

            repository.save(submission);

            if (dto.isFinalStep()) {
                return handleFinalStep(dto.getSessionId());
            }

            return SuccessEnum.CREATED.getSuccessResponseBody("Step saved");
        } catch (Exception e) {
            log.error("Save onboarding step error: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<?> handleFinalStep(String sessionId) {
        try {
            List<OnboardingFormSubmission> submissions = repository.findAllBySessionIdOrderByStep(sessionId);
            if (submissions.isEmpty()) throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("No submission found"), HttpStatus.BAD_REQUEST);

            String scenarioId = submissions.get(0).getScenarioId();
            Map<String, List<String>> requiredFieldsByForm = formValidationCacheService.getValidationConfig(scenarioId);

            Map<String, Object> merged = new LinkedHashMap<>();
            for (OnboardingFormSubmission sub : submissions) {
                Map<String, Object> data = objectMapper.readValue(sub.getFormData(), new TypeReference<>() {});
                validateFormFields(sub.getFormId(), data, requiredFieldsByForm);
                merged.putAll(data);
            }

            // 🧠 Map merged -> UserDto (you might use a mapper utility)
            UserDto userDto = objectMapper.convertValue(merged, UserDto.class);

            //Need to configure dynamic onboarding and other configurations.
            //userService.save(userDto);

            repository.deleteAll(submissions);
            formValidationCacheService.evict(scenarioId);

            return SuccessEnum.SUCCESS.getSuccessResponseBody("User Registered");

        } catch (Exception e) {
            log.error("handleFinalStep error: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Merge/Validation failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFormFields(String formId, Map<String, Object> data, Map<String, List<String>> requiredFieldsByForm) {
        List<String> required = requiredFieldsByForm.getOrDefault(formId, Collections.emptyList());
        for (String field : required) {
            if (!data.containsKey(field) || Objects.isNull(data.get(field))) {
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Missing required field: " + field), HttpStatus.BAD_REQUEST);
            }
        }
    }
}