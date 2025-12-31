package com.photon.alerts.service;

import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.dto.request.AlertRequestDto;
import com.photon.alerts.dto.request.AlertTemplateRequestDto;
import com.photon.alerts.dto.response.AlertGroupedResponseDto;
import com.photon.alerts.dto.response.AlertResponseDto;
import com.photon.alerts.entity.Alert;
import com.photon.alerts.entity.AlertTemplate;
import com.photon.alerts.entity.RemoteAttachment;
import com.photon.alerts.dto.mapper.AlertMapper;
import com.photon.alerts.dto.mapper.AlertTemplateMapper;
import com.photon.alerts.dto.mapper.RemoteAttachmentMapper;
import com.photon.alerts.repository.AlertRepository;
import com.photon.alerts.repository.AlertTemplateRepository;
import com.photon.alerts.repository.RemoteAttachmentRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertTemplateRepository alertTemplateRepository;
    private final RemoteAttachmentRepository remoteAttachmentRepository;
    private final TransactionalOperator transactionalOperator;

    public AlertService(AlertRepository alertRepository, AlertTemplateRepository alertTemplateRepository, RemoteAttachmentRepository remoteAttachmentRepository, TransactionalOperator transactionalOperator) {
        this.alertRepository = alertRepository;
        this.alertTemplateRepository = alertTemplateRepository;
        this.remoteAttachmentRepository = remoteAttachmentRepository;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<AlertResponseDto> getAlertByAlertTypeAndAlertSubType(String alertType, String alertSubType) throws ApplicationException {
        return alertRepository.findByAlertTypeAndAlertSubType(alertType, alertSubType)
                .flatMap(alert -> alertTemplateRepository.findByAlertId(alert.getId())
                        .collectList()
                        .map(templates -> AlertResponseDto.toDto(alert, templates))
                );
    }

    public Mono<ApiResponseDto<AlertResponseDto>> createAlert(AlertRequestDto request) {
        Alert alert = AlertMapper.toEntity(request);

        Mono<ApiResponseDto<AlertResponseDto>> transactionalFlow = alertRepository.save(alert)
                .flatMap(savedAlert -> {
                    List<AlertTemplateRequestDto> templateDtos = request.getTemplates();
                    if (templateDtos == null || templateDtos.isEmpty()) {
                        return mapToResponseMono(savedAlert).map(SuccessEnum.CREATED::getSuccessResponseBody);
                    }

                    List<AlertTemplate> templateEntities = AlertTemplateMapper.toEntityList(templateDtos, alert);

                    return Flux.fromIterable(templateEntities)
                            .flatMap(alertTemplateRepository::save)
                            .collectList()
                            .flatMap(savedTemplates -> {
                                List<RemoteAttachment> allAttachments = new ArrayList<>();

                                for (int i = 0; i < savedTemplates.size(); i++) {
                                    AlertTemplate savedTemplate = savedTemplates.get(i);
                                    List<RemoteAttachmentDto> attachDtos = templateDtos.get(i).getMailAttachments();

                                    if (attachDtos != null && !attachDtos.isEmpty()) {
                                        allAttachments.addAll(RemoteAttachmentMapper.toEntityList(attachDtos, savedTemplate.getId()));
                                    }
                                }

                                return remoteAttachmentRepository.saveAll(allAttachments)
                                        .then(mapToResponseMono(savedAlert))
                                        .map(SuccessEnum.CREATED::getSuccessResponseBody);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Exception in createAlert: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to Create Alert: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
                });

        return transactionalOperator.transactional(transactionalFlow);
    }

    public Mono<ApiResponseDto<AlertResponseDto>> getById(Long id) {
        return alertRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Alert not found")))
                .flatMap(this::mapToResponseMono)
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<List<AlertGroupedResponseDto>>> getAll(int pageNumber, int pageSize) {
        return alertRepository.findAll()
                .flatMap(this::mapToResponseMono)
                .collectList()
                .map(alerts -> {

                    Map<String, List<AlertResponseDto>> grouped = alerts.stream()
                            .collect(Collectors.groupingBy(AlertResponseDto::getAlertType));

                    return grouped.entrySet().stream()
                            .map(entry -> AlertGroupedResponseDto.builder()
                                    .alertType(entry.getKey()).alerts(entry.getValue()).build())
                            .sorted(Comparator.comparing(AlertGroupedResponseDto::getAlertType))
                            .toList();
                })
                .flatMap(groupedList -> {
                    int total = groupedList.size();
                    int start = Math.min(pageNumber * pageSize, total);
                    int end = Math.min(start + pageSize, total);
                    List<AlertGroupedResponseDto> paged = groupedList.subList(start, end);

                    return Mono.just(SuccessEnum.SUCCESS.getSuccessResponseBody(paged, null, pageNumber,
                            pageSize, (int) Math.ceil((double) total / pageSize), total
                    ));
                });
    }


    public Mono<ApiResponseDto<AlertResponseDto>> updateAlert(Long id, AlertRequestDto request) {
        Mono<ApiResponseDto<AlertResponseDto>> transactionalFlow = alertRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApplicationException(
                        ExceptionEnum.ERR_1001.getErrorResponseBody("Alert not found"), HttpStatus.NOT_FOUND)))
                .flatMap(existing -> {
                    existing.setAlertType(request.getAlertType());
                    existing.setAlertSubType(request.getAlertSubType());
                    existing.setAudience(request.getAudience());
                    existing.setTopic(request.getTopic());
                    existing.setModifiedAt(LocalDateTime.now());
                    return alertRepository.save(existing);
                })
                .flatMap(updatedAlert ->
                        alertTemplateRepository.findByAlertId(updatedAlert.getId())
                                .collectList()
                                .flatMap(existingTemplates -> {
                                    List<AlertTemplateRequestDto> templateDtos = request.getTemplates();

                                    if (existingTemplates.isEmpty()) {
                                        return saveNewTemplatesAndAttachments(updatedAlert, templateDtos)
                                                .then(mapToResponseMono(updatedAlert))
                                                .map(SuccessEnum.UPDATED::getSuccessResponseBody);
                                    }

                                    List<Mono<Void>> deleteMonos = existingTemplates.stream()
                                            .flatMap(template -> Stream.of(
                                                    remoteAttachmentRepository.findByTemplateId(template.getId())
                                                            .flatMap(att -> remoteAttachmentRepository.deleteById(att.getId()))
                                                            .then(),
                                                    alertTemplateRepository.deleteById(template.getId())
                                            )).collect(Collectors.toList());

                                    return Flux.concat(deleteMonos)
                                            .then(saveNewTemplatesAndAttachments(updatedAlert, templateDtos))
                                            .then(mapToResponseMono(updatedAlert))
                                            .map(SuccessEnum.UPDATED::getSuccessResponseBody);
                                })
                )
                .onErrorResume(e -> {
                    log.error("Exception in updateAlert: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(
                            ExceptionEnum.ERR_1000.getErrorResponseBody("Update failed: " + e.getMessage()),
                            HttpStatus.INTERNAL_SERVER_ERROR));
                });

        return transactionalOperator.transactional(transactionalFlow);
    }


    public Mono<ApiResponseDto<AlertResponseDto>> patchAlert(Long id, AlertRequestDto patchDto) {
        return alertRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Alert not found")))
                .flatMap(alert -> {
                    if (patchDto.getAlertType() != null) alert.setAlertType(patchDto.getAlertType());
                    if (patchDto.getAlertSubType() != null) alert.setAlertSubType(patchDto.getAlertSubType());
                    if (patchDto.getTopic() != null) alert.setTopic(patchDto.getTopic());
                    alert.setModifiedAt(LocalDateTime.now());
                    return alertRepository.save(alert);
                })
                .flatMap(updated -> mapToResponseMono(updated).map(SuccessEnum.UPDATED::getSuccessResponseBody));
    }


    public Mono<ApiResponseDto<?>> deleteAlert(Long id) {
        return alertRepository.deleteById(id).thenReturn(SuccessEnum.DELETED.getSuccessResponseBody());
    }


    private Mono<Void> saveNewTemplatesAndAttachments(Alert updatedAlert, List<AlertTemplateRequestDto> templateDtos) {
        if (templateDtos == null || templateDtos.isEmpty()) {
            return Mono.empty();
        }

        List<AlertTemplate> templateEntities = AlertTemplateMapper.toEntityList(templateDtos, updatedAlert);

        return Flux.fromIterable(templateEntities)
                .flatMap(alertTemplateRepository::save)
                .collectList()
                .flatMap(savedTemplates -> {
                    List<RemoteAttachment> allAttachments = new ArrayList<>();

                    for (int i = 0; i < savedTemplates.size(); i++) {
                        AlertTemplate savedTemplate = savedTemplates.get(i);
                        List<RemoteAttachmentDto> attachDtos = templateDtos.get(i).getMailAttachments();

                        if (attachDtos != null && !attachDtos.isEmpty()) {
                            allAttachments.addAll(RemoteAttachmentMapper.toEntityList(attachDtos, savedTemplate.getId()));
                        }
                    }

                    return remoteAttachmentRepository.saveAll(allAttachments).then();
                });
    }


    //----------------------- MAPPING HELPERS -----------------------//
    private Mono<AlertResponseDto> mapToResponseMono(Alert alert) {
        return alertTemplateRepository.findByAlertId(alert.getId())
                .flatMap(template ->
                        remoteAttachmentRepository.findByTemplateId(template.getId())
                                .collectList()
                                .map(attachments -> AlertTemplateMapper.toDto(template, attachments))
                )
                .collectList()
                .map(templateDtos -> AlertMapper.toDto(alert, templateDtos));
    }
}