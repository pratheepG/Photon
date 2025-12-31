package com.photon.identity.onboarding.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.onboarding.dto.ScenarioFormStepDto;
import com.photon.identity.onboarding.dto.ScenarioRequestDto;
import com.photon.identity.onboarding.dto.ScenarioResponseDto;
import com.photon.identity.onboarding.dto.mapper.ScenarioMapper;
import com.photon.identity.onboarding.entity.Scenario;
import com.photon.identity.onboarding.entity.ScenarioForm;
import com.photon.identity.onboarding.repository.ScenarioFormRepository;
import com.photon.identity.onboarding.repository.ScenarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioFormRepository scenarioFormRepository;

    public ApiResponseDto<List<ScenarioResponseDto>> getAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Scenario> scenarios = scenarioRepository.findAll(pageable);

            List<String> scenarioIds = scenarios.getContent().stream()
                    .map(Scenario::getId)
                    .collect(Collectors.toList());

            List<ScenarioForm> allForms = scenarioFormRepository.findByScenarioIdIn(scenarioIds);

            Map<String, List<ScenarioForm>> formsGrouped = allForms.stream()
                    .collect(Collectors.groupingBy(ScenarioForm::getScenarioId));

            List<ScenarioResponseDto> dtoList = scenarios.getContent().stream()
                    .map(scenario -> {
                        List<ScenarioForm> forms = formsGrouped.getOrDefault(scenario.getId(), Collections.emptyList());
                        return ScenarioMapper.toDto(scenario, forms);
                    })
                    .collect(Collectors.toList());

            return SuccessEnum.SUCCESS.getSuccessResponseBody(dtoList, null, page, dtoList.size(), scenarios.getTotalPages(), (int) scenarios.getTotalElements());

        } catch (Exception e) {
            log.error("Exception in getAllScenarios: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<ScenarioResponseDto> getById(String id) {
        try {
            Scenario scenario = scenarioRepository.findById(id)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Scenario not found"), HttpStatus.NOT_FOUND));

            List<ScenarioForm> scenarioForms = scenarioFormRepository.findByScenarioId(id);

            ScenarioResponseDto dto = ScenarioMapper.toDto(scenario, scenarioForms);

            return SuccessEnum.SUCCESS.getSuccessResponseBody(dto);

        } catch (ApplicationException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getScenarioById: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> delete(List<String> ids) {
        try {
            scenarioRepository.deleteAllById(ids);
            if (!scenarioRepository.findAllById(ids).isEmpty()) {
                throw new ApplicationException(ExceptionEnum.ERR_1011.getErrorResponseBody(),
                        HttpStatus.BAD_REQUEST);
            }
            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Exception in deleteScenarios: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> create(ScenarioRequestDto requestDto) {
        try {
            Scenario scenario = ScenarioMapper.toEntity(requestDto);
            scenarioRepository.save(scenario);

            for (ScenarioFormStepDto stepDto : requestDto.getForms()) {
                ScenarioForm scenarioForm = ScenarioForm.builder()
                        .name(stepDto.getName())
                        .step(stepDto.getStep())
                        .scenarioId(stepDto.getScenarioId())
                        .formId(stepDto.getFormId())
                        .submissionState(stepDto.getSubmissionState())
                        .build();

                scenarioFormRepository.save(scenarioForm);
            }

            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (Exception e) {
            log.error("Exception in createScenario: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> createBulk(List<ScenarioRequestDto> dtoList) {
        try {
            List<Scenario> scenarioList = dtoList.stream()
                    .map(ScenarioMapper::toEntity)
                    .toList();

            List<ScenarioForm> scenarioForms = new ArrayList<>();

            for (ScenarioRequestDto dto : dtoList) {
                for (ScenarioFormStepDto formDto : dto.getForms()) {
                    scenarioForms.add(ScenarioForm.builder()
                            .step(formDto.getStep())
                            .submissionState(formDto.getSubmissionState())
                            .name(formDto.getName())
                            .scenarioId(formDto.getScenarioId())
                            .formId(formDto.getFormId())
                            .build());
                }
            }

            scenarioRepository.saveAll(scenarioList);
            scenarioFormRepository.saveAll(scenarioForms);

            return SuccessEnum.CREATED.getSuccessResponseBody();

        } catch (Exception e) {
            log.error("Bulk create scenario exception: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}