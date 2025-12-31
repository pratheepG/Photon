package com.photon.enums;

import com.photon.constants.ResponseConstant;
import com.photon.dto.ApiResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public enum SuccessEnum {

    SUCCESS(ResponseConstant.SUCCESS),
    CREATED(ResponseConstant.CREATED),
    UPDATED(ResponseConstant.UPDATED),
    DELETED(ResponseConstant.DELETED),
    OTP_SENT_SUCCESS(ResponseConstant.OTP_SENT_SUCCESS),
    OTP_VALIDATED(ResponseConstant.OTP_VALIDATED);

    private final String message;

    SuccessEnum(String message) {
        this.message = message;
    }

    public ApiResponseDto<?> getSuccessResponseBody() {
        return buildSuccessResponseBody(this.message);
    }

    public <T> ApiResponseDto<T> getSuccessResponseBody(T data) {
        return buildSuccessResponseBody(data, this.message);
    }

    public ApiResponseDto<?> getSuccessResponseBody(final String customMessage) {
        return buildSuccessResponseBody(customMessage);
    }

    public <T> ApiResponseDto<T> getSuccessResponseBody(T data, String messageDetail) {
        return buildSuccessResponseBody(data, messageDetail);
    }

    @Deprecated
    public <T> ApiResponseDto<T> getSuccessResponseBody(T data, final String messageDetail, int pageNumber, int pageSize, int totalPages ) {
        return buildSuccessResponseBody(data, messageDetail, pageNumber, pageSize, totalPages);
    }

    @Deprecated
    public <T> ApiResponseDto<T> getSuccessResponseBody(T data, final String messageDetail, int pageNumber, int pageSize, int totalPages, int totalRecords ) {
        return buildSuccessResponseBody(data, messageDetail, pageNumber, pageSize, totalPages, totalRecords);
    }

    public <T> ApiResponseDto<List<T>> getSuccessResponseBody(Page<T> data) {
        return buildSuccessResponseBody(data.getContent(), this.message, data.getNumber(), data.getSize(), data.getTotalPages(), (int) data.getTotalElements());
    }

    public <T> ApiResponseDto<List<T>> getSuccessResponseBody(Page<T> data, final String messageDetail) {
        return buildSuccessResponseBody(data.getContent(), messageDetail, data.getNumber(), data.getSize(), data.getTotalPages(), (int) data.getTotalElements());
    }

    private ApiResponseDto<?> buildSuccessResponseBody(String messageDetail) {
        String finalMessage = StringUtils.hasText(messageDetail) ? messageDetail : this.message;

        return new ApiResponseDto.builder<>()
                .opStatus(0)
                .success(true)
                .message(finalMessage)
                .build();
    }

    private <T> ApiResponseDto<T> buildSuccessResponseBody(T responseData, String messageDetail) {
        String finalMessage = StringUtils.hasText(messageDetail) ? messageDetail : this.message;

        return ApiResponseDto.<T>builder()
                .responseData(responseData)
                .message(finalMessage)
                .opStatus(0)
                .success(true)
                .timeStamp(LocalDateTime.now().toString())
                .build();
    }

    private <T> ApiResponseDto<T> buildSuccessResponseBody(T responseData, String messageDetail, int pageNumber, int pageSize, int totalPages) {
        String finalMessage = StringUtils.hasText(messageDetail) ? messageDetail : this.message;

        return ApiResponseDto.<T>builder()
                .responseData(responseData)
                .message(finalMessage)
                .opStatus(0)
                .success(true)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .timeStamp(LocalDateTime.now().toString())
                .build();
    }

    private <T> ApiResponseDto<T> buildSuccessResponseBody(T responseData, String messageDetail, int pageNumber, int pageSize, int totalPages, int totalRecords) {
        String finalMessage = StringUtils.hasText(messageDetail) ? messageDetail : this.message;

        return ApiResponseDto.<T>builder()
                .responseData(responseData)
                .message(finalMessage)
                .opStatus(0)
                .success(true)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalRecords(totalRecords)
                .timeStamp(LocalDateTime.now().toString())
                .build();
    }


}