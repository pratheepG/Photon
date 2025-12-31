package com.photon.exception;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestController
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public final ResponseEntity<ApiResponseDto<?>> handleApplicationException(ApplicationException ex, WebRequest request) {
        return ResponseEntity.status(ex.getHttpStatus()).body(ex.getApiResponseDto());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiResponseDto<?>> handleGlobalException(Exception ex, WebRequest request) {
        return ResponseEntity.status(400).body(ExceptionEnum.ERR_1003.getErrorResponseBody(ex.getMessage()));
    }

}