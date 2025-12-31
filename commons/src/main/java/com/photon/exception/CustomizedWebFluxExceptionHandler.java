package com.photon.exception;

import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

import java.io.IOException;

@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class CustomizedWebFluxExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public Mono<ResponseEntity<ApiResponseDto<?>>> handleApplicationException(ApplicationException ex) {
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(ex.getApiResponseDto()));
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public Mono<ResponseEntity<ApiResponseDto<?>>> handleUnsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException ex) {
        ApiResponseDto<?> response = ExceptionEnum.ERR_1042.getErrorResponseBody(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response));
    }

    @ExceptionHandler(IOException.class)
    public Mono<ResponseEntity<ApiResponseDto<?>>> handleIOException(IOException ex) {
        ApiResponseDto<?> response = ExceptionEnum.ERR_1044.getErrorResponseBody(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponseDto<?>>> handleGenericException(Exception ex) {
        ApiResponseDto<?> response = ExceptionEnum.ERR_1000.getErrorResponseBody(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}