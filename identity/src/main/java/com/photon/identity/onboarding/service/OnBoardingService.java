//package com.photon.identity.onboarding.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.photon.dto.ApiResponseDto;
//import com.photon.enums.ExceptionEnum;
//import com.photon.enums.SuccessEnum;
//import com.photon.exception.ApplicationException;
//import com.photon.identity.onboarding.repository.OnBoardingConfigRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class OnBoardingConfigService {
//
//    private final OnBoardingConfigRepository onBoardingRepository;
//
//    public OnBoardingConfigService(OnBoardingConfigRepository onBoardingRepository) {
//        this.onBoardingRepository = onBoardingRepository;
//    }
//
//    public ApiResponseDto<?> create(FieldRequestDto dto) {
//        try {
//
////            String json = objectMapper.writeValueAsString(concreteConfig);
////
////            Field field = Field.builder().name(dto.getName()).isCollection(isCollection)
////                    .type(dto.getType()).field(json).build();
////
////            this.onBoardingRepository.save(field);
//            return SuccessEnum.CREATED.getSuccessResponseBody();
//
//        } catch (JsonProcessingException e) {
//            log.error("JSON processing error: {}", e.getMessage());
//            throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("Invalid payload"), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            log.error("Exception while saving onboarding config: {}", e.getMessage());
//            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}