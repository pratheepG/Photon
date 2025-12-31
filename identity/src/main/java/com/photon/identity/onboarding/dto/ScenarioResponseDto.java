package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.ApprovalType;
import com.photon.identity.onboarding.enums.ScenarioSubmissionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResponseDto {
    private String id;
    private String name;
    private ScenarioSubmissionType submissionType;
    private ApprovalType approvalType;
    private List<ScenarioFormStepDto> formSteps;
}