package com.photon.alerts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.alerts.repository.AlertTemplateRepository;
import com.photon.alerts.utils.TemplateProcessor;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.SuccessEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

//@Service
//public class AlertManagerService {
//
//    private final AlertTemplateRepository alertTemplateRepository;
//    private final ObjectMapper objectMapper;
//
//    public AlertManagerService(AlertTemplateRepository alertTemplateRepository, ObjectMapper objectMapper) {
//        this.alertTemplateRepository = alertTemplateRepository;
//        this.objectMapper = objectMapper;
//    }
//
//    public Mono<ApiResponseDto<?>> sendAlert(AlertRequestDto alertRequest) {
//
//
//
//        return alertTemplateRepository.findById("")
//                .flatMap(template -> {
//                    String subject = TemplateProcessor.resolveTemplate(template.getSubjectTemplate(), alertRequest.getPlaceholders());
//                    String message = TemplateProcessor.resolveTemplate(template.getMessageTemplate(), alertRequest.getPlaceholders());
//                    return Mono.just(SuccessEnum.CREATED.getSuccessResponseBody());
//                });
//    }
//}