package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.ScenarioSubmissionState;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingSubmissionRequestDto {
    private String sessionId;
    private String scenarioId;
    private String formId;
    private int step;
    private boolean finalStep;
    private ScenarioSubmissionState submissionState;
    private Map<String, Object> formData;
}