package com.photon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private int opStatus;
    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String timeStamp;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int pageNumber;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int pageSize;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int totalRecords;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int totalPages;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int errCode;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String errMessage;

    private String message;
    private T responseData;

    public ApiResponseDto(int opStatus, boolean success, String message) {
        this.opStatus = opStatus;
        this.success = success;
        this.message = message;
    }

    public static class builder<T> {
        private final ApiResponseDto<T> dto;

        public builder() {
            dto = new ApiResponseDto<>();
        }

        public builder<T> responseData(T responseData) {
            dto.setResponseData(responseData);
            return this;
        }

        public builder<T> message(String message) {
            dto.setMessage(message);
            return this;
        }

        public builder<T> errMessage(String err) {
            dto.setErrMessage(err);
            return this;
        }

        public builder<T> errCode(int errCode) {
            dto.setErrCode(errCode);
            return this;
        }

        public builder<T> timeStamp(String timeStamp) {
            dto.setTimeStamp(timeStamp);
            return this;
        }

        public builder<T> pageNumber(int pageNumber) {
            dto.setPageNumber(pageNumber);
            return this;
        }

        public builder<T> pageSize(int pageSize) {
            dto.setPageSize(pageSize);
            return this;
        }

        public builder<T> totalRecords(int totalRecords) {
            dto.setTotalRecords(totalRecords);
            return this;
        }

        public builder<T> totalPages(int totalPages) {
            dto.setTotalPages(totalPages);
            return this;
        }

        public builder<T> opStatus(int opStatus) {
            dto.setOpStatus(opStatus);
            return this;
        }

        public builder<T> success(boolean success) {
            dto.setSuccess(success);
            return this;
        }

        public ApiResponseDto<T> build() {
            return dto;
        }
    }

    public ApiResponseDto<T> page(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public ApiResponseDto<T> size(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ApiResponseDto<T> totalPages(int totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public ApiResponseDto<T> totalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
        return this;
    }
}