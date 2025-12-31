package com.photon.enums;

import com.photon.constants.ResponseConstant;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.ExceptionDto;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Getter
public enum ExceptionEnum {
    /**
     * INTERNAL_SERVER_ERROR
     */
    ERR_1000(1000, ResponseConstant.INTERNAL_SERVER_ERROR),
    /**
     * TOKEN_NOT_FOUND
     */
    ERR_1001(1001, ResponseConstant.TOKEN_NOT_FOUND),
    /**
     * EXPIRED_JWT_TOKEN
     */
    ERR_1002(1002, ResponseConstant.EXPIRED_JWT_TOKEN),
    /**
     * INVALID_AUTH_TYPE
     */
    ERR_1003(1003, ResponseConstant.INVALID_AUTH_TYPE),
    /**
     * FAILED_TO_AUTHENTICATE
     */
    ERR_1004(1004, ResponseConstant.FAILED_TO_AUTHENTICATE),
    /**
     * CERTIFICATE_NOT_FOUND
     */
    ERR_1005(1005, ResponseConstant.CERTIFICATE_NOT_FOUND),
    /**
     * RECORD_NOT_FOUND_FOR_ID
     */
    ERR_1006(1006, ResponseConstant.RECORD_NOT_FOUND_FOR_ID),
    /**
     * FAILED_TO_CREATE
     */
    ERR_1007(1007, ResponseConstant.FAILED_TO_CREATE),
    /**
     * FAILED_TO_UPDATE
     */
    ERR_1008(1008, ResponseConstant.FAILED_TO_UPDATE),
    /**
     * FAILED_TO_GENERATE_ENDPOINT_INFO
     */
    ERR_1009(1009, ResponseConstant.FAILED_TO_GENERATE_ENDPOINT_INFO),
    /**
     * BAD_REQUEST_PARAM
     */
    ERR_1010(1010, ResponseConstant.BAD_REQUEST_PARAM),
    /**
     * FAILED_TO_DELETE
     */
    ERR_1011(1011, ResponseConstant.FAILED_TO_DELETE),
    /**
     * RECORD_ALREADY_EXIST
     */
    ERR_1012(1012, ResponseConstant.RECORD_ALREADY_EXIST),
//    /**
//     * FAILED_TO_DELETE
//     */
//    ERR_1013(1013, ResponseConstant.FAILED_TO_DELETE),
    /**
     * UN_AUTHORIZED
     */
    ERR_1014(1014, ResponseConstant.UN_AUTHORIZED),
    /**
     * OTP_SENT_FAILED
     */
    ERR_1015(1015, ResponseConstant.OTP_SENT_FAILED),
    /**
     * IDENTITY_PROVIDER_ERROR
     */
    ERR_1016(1016, ResponseConstant.IDENTITY_PROVIDER_ERROR),
    /**
     * AUTH_TYPE_NOT_FOUND
     */
    ERR_1017(1017, ResponseConstant.AUTH_TYPE_NOT_FOUND),
    /**
     * INACTIVE_SECOND_FACTOR_AUTH
     */
    ERR_1018(1018, ResponseConstant.INACTIVE_SECOND_FACTOR_AUTH),
    /**
     * FAILED_TO_ACTIVATE_USER_AUTH
     */
    ERR_1019(1019, ResponseConstant.FAILED_TO_ACTIVATE_USER_AUTH),

    /**
     * USER_NOT_FOUND
     */
    ERR_1020(1020, ResponseConstant.USER_NOT_FOUND),
    /**
     * USER_DISABLED
     */
    ERR_1021(1021, ResponseConstant.USER_DISABLED),
    /**
     * SUSPICIOUS_USER_ACTIVITY
     */
    ERR_1022(1022, ResponseConstant.SUSPICIOUS_USER_ACTIVITY),
    /**
     * REACHED_MAXIMUM_OTP_REQUEST
     */
    ERR_1023(1023, ResponseConstant.REACHED_MAXIMUM_OTP_REQUEST),
    /**
     * USER_SESSION_NOT_FOUND
     */
    ERR_1024(1024, ResponseConstant.USER_SESSION_NOT_FOUND),
    /**
     * UN_SUPPORTED_AUTH_TYPE
     */
    ERR_1025(1025, ResponseConstant.UN_SUPPORTED_AUTH_TYPE),
    /**
     * FAILED_TO_MAP_USER_DEVICE
     */
    ERR_1026(1026, ResponseConstant.FAILED_TO_MAP_USER_DEVICE),
    /**
     * FAILED_REGISTER_DEVICE
     */
    ERR_1027(1027, ResponseConstant.FAILED_REGISTER_DEVICE),
    /**
     * INVALID_REPORTING_PARAMS
     */
    ERR_1028(1028, ResponseConstant.INVALID_REPORTING_PARAMS),
    /**
     * UN_KNOWN_DEVICE
     */
    ERR_1029(1029, ResponseConstant.UN_KNOWN_DEVICE),
    /**
     * INVALID_CREDENTIALS
     */
    ERR_1030(1030, ResponseConstant.INVALID_CREDENTIALS),
    /**
     * FAILED_TO_LOGOUT
     */
    ERR_1031(1031, ResponseConstant.FAILED_TO_LOGOUT),
    /**
     * FAILED_TO_PARSE_DATA
     */
    ERR_1032(1032, ResponseConstant.FAILED_TO_PARSE_DATA),
    /**
     * INVALID_IDP
     */
    ERR_1033(1033, ResponseConstant.INVALID_IDP),
    /**
     * USER_ALREADY_EXIST
     */
    ERR_1034(1034, ResponseConstant.USER_ALREADY_EXIST),
    /**
     * INVALID_JWT_HEADER
     */
    ERR_1035(1035, ResponseConstant.INVALID_JWT_HEADER),
    /**
     * USER_PERMISSION_DENIED
     */
    ERR_1036(1036, ResponseConstant.USER_PERMISSION_DENIED),
    /**
     * INVALID_JWT_TOKEN
     */
    ERR_1037(1037, ResponseConstant.INVALID_JWT_TOKEN),
    /**
     * INACTIVE_AUTH_TYPE
     */
    ERR_1038(1038, ResponseConstant.INACTIVE_AUTH_TYPE),
    /**
     * FAILED_TO_UPLOAD_FILE
     */
    ERR_1039(1039, ResponseConstant.FAILED_TO_UPLOAD_FILE),
    /**
     * FILE_SIZE_EXCEEDED
     */
    ERR_1040(1040, ResponseConstant.FILE_SIZE_EXCEEDED),
    /**
     * INVALID_HEADER_PARAM
     */
    ERR_1041(1041, ResponseConstant.INVALID_HEADER_PARAM),
    /**
     * UN_SUPPORTED_FILE_TYPE
     */
    ERR_1042(1042, ResponseConstant.UN_SUPPORTED_FILE_TYPE),
    /**
     * INVALID_CREDENTIAL
     */
    ERR_1043(1043, ResponseConstant.INVALID_CREDENTIAL),
    /**
     * UNABLE_TO_REACH_SERVER
     */
    ERR_1044(1044, ResponseConstant.UNABLE_TO_REACH_SERVER),
    /**
     * SERVICE_UNAVAILABLE
     */
    ERR_1045(1045, ResponseConstant.SERVICE_UNAVAILABLE),
    /**
     * EXPIRED_CERTIFICATE
     */
    ERR_1046(1046, ResponseConstant.EXPIRED_CERTIFICATE),
    /**
     * NEED_AT_LEAST_ONE_USER_IDENTIFIER
     */
    ERR_1047(1047, ResponseConstant.NEED_AT_LEAST_ONE_USER_IDENTIFIER),
    /**
     * USER_NAME_ALREADY_EXIST
     */
    ERR_1048(1048, ResponseConstant.USER_NAME_ALREADY_EXIST),
    /**
     * PHONE_NUMBER_ALREADY_EXIST
     */
    ERR_1049(1049, ResponseConstant.PHONE_NUMBER_ALREADY_EXIST),
    /**
     * EMAIL_ALREADY_EXIST
     */
    ERR_1050(1050, ResponseConstant.EMAIL_ALREADY_EXIST),
    /**
     * INVALID_REQ_PAYLOAD
     */
    ERR_1051(1051, ResponseConstant.INVALID_REQ_PAYLOAD),
    /**
     * EXPIRED_PASSWORD
     */
    ERR_1052(1052, ResponseConstant.EXPIRED_PASSWORD);

    final int errCode;
    final String message;

    ExceptionEnum(int i, String s) {
        this.errCode = i;
        this.message = s;
    }

    /**
     * @return ApiResponseDto<?>
     */
    public ApiResponseDto<?> getErrorResponseBody() {
        return buildErrorResponseBody(this.message);
    }

    /**
     * @param customMessage String
     * @return ApiResponseDto<?>
     */
    public ApiResponseDto<?> getErrorResponseBody(final String customMessage) {
        return buildErrorResponseBody(customMessage);
    }

    /**
     * @param customMessage String
     * @param obj Object
     * @return ApiResponseDto<?>
     */
    public ApiResponseDto<?> getErrorResponseBody(final String customMessage, Object obj) {
        return buildErrorResponseBody(customMessage, obj);
    }

    /**
     * @param obj Object
     * @return ApiResponseDto<?>
     */
    public ApiResponseDto<?> getErrorResponseBody(Object obj) {
        return buildErrorResponseBody(this.message, obj);
    }

    private ApiResponseDto<?> buildErrorResponseBody(String messageDetail) {
        String finalMessage = StringUtils.hasText(messageDetail) ? messageDetail : this.message;
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(finalMessage)
                .timestamp(LocalDateTime.now())
                .build();

        return new ApiResponseDto.builder<ExceptionDto>()
                .responseData(exceptionDto)
                .timeStamp(String.valueOf(exceptionDto.getTimestamp()))
                .opStatus(-1)
                .success(false)
                .errCode(this.errCode)
                .errMessage(this.message)
                .build();
    }

    private ApiResponseDto<?> buildErrorResponseBody(String messageDetail, Object obj) {
        String finalMessage = StringUtils.hasText(messageDetail) ? messageDetail : this.message;

        return new ApiResponseDto.builder<>()
                .responseData(obj)
                .timeStamp(String.valueOf(LocalDateTime.now()))
                .opStatus(-1)
                .success(false)
                .errCode(this.errCode)
                .errMessage(finalMessage)
                .build();
    }
}