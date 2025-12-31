package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.ScenarioSubmissionState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioFormStepDto {
    private Long id;
    private String name;
    private int step;
    private ScenarioSubmissionState submissionState;
    private String scenarioId;
    private String formId;
}