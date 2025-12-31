package com.photon.identity.onboarding.dto.mapper;

import com.photon.identity.onboarding.dto.ScenarioFormStepDto;
import com.photon.identity.onboarding.dto.ScenarioRequestDto;
import com.photon.identity.onboarding.dto.ScenarioResponseDto;
import com.photon.identity.onboarding.entity.Scenario;
import com.photon.identity.onboarding.entity.ScenarioForm;

import java.util.List;
import java.util.stream.Collectors;

public class ScenarioMapper {

    public static ScenarioResponseDto toDto(Scenario scenario, List<ScenarioForm> scenarioForms) {
        ScenarioResponseDto dto = new ScenarioResponseDto();
        dto.setId(scenario.getId());
        dto.setName(scenario.getName());
        dto.setApprovalType(scenario.getApprovalType());
        dto.setSubmissionType(scenario.getSubmissionType());

        List<ScenarioFormStepDto> formSteps = scenarioForms.stream()
                .map(form -> new ScenarioFormStepDto(form.getId(), form.getName(), form.getStep(),
                        form.getSubmissionState(), form.getScenarioId(), form.getFormId()))
                .collect(Collectors.toList());

        dto.setFormSteps(formSteps);
        return dto;
    }


    public static Scenario toEntity(ScenarioRequestDto dto) {
        return Scenario.builder().id(dto.getId()).name(dto.getName()).submissionType(dto.getSubmissionType())
                .approvalType(dto.getApprovalType()).build();
    }
}