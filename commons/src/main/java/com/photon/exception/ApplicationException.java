package com.photon.exception;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.ExceptionDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException{

    private final ApiResponseDto<?> apiResponseDto;
    private final HttpStatus httpStatus;

    public ApplicationException(ApiResponseDto<?> apiResponseDto, HttpStatus httpStatus) {
        super(getMessageFromResponse(apiResponseDto));
        this.apiResponseDto = apiResponseDto;
        this.httpStatus = httpStatus;
    }

    private static String getMessageFromResponse(ApiResponseDto<?> apiResponseDto) {
        if (apiResponseDto != null && apiResponseDto.getResponseData() instanceof ExceptionDto ex) {
            return ex.getMessage();
        }
        return "Unknown error";
    }
}