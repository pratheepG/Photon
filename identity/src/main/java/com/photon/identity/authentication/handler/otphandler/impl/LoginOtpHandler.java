package com.photon.identity.authentication.handler.otphandler.impl;

import com.photon.constants.ApplicationConstants;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.dto.UserLoginHistoryDto;
import com.photon.identity.authentication.dto.mapper.UserMapper;
import com.photon.identity.authentication.dto.request.LoginOtpDto;
import com.photon.identity.authentication.handler.otphandler.api.OtpHandler;
import com.photon.identity.authentication.repository.UserRepository;
import com.photon.identity.authentication.service.PasswordService;
import com.photon.identity.authentication.service.UserLoginHistoryService;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.dto.mapper.AuthTypeMapper;
import com.photon.identity.idp.repository.AuthTypeRepository;
import com.photon.utils.HttpRequestManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("loginOtpHandler")
public class LoginOtpHandler implements OtpHandler {

    private final AuthTypeRepository authTypeRepository;
    private final UserLoginHistoryService userLoginHistoryService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public LoginOtpHandler(AuthTypeRepository authTypeRepository, UserLoginHistoryService userLoginHistoryService, UserRepository userRepository, PasswordService passwordService) {
        this.authTypeRepository = authTypeRepository;
        this.userLoginHistoryService = userLoginHistoryService;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public ApiResponseDto<AuthResponseDto> requestOtp(String authType, IdentityProviderDto identityProviderDto, Object authRequest) throws ApplicationException {
        AuthResponseDto authResponse = new AuthResponseDto();
        UserLoginHistoryDto userLogin;
        Optional<UserDto> user = Optional.empty();
        ReportingParamsDto reportingParams = HttpRequestManager.getReportingParams();
        Optional<AuthTypeDto> authTypeOptional =  this.authTypeRepository.findById(authType).map(AuthTypeMapper::toDto);

        if (authTypeOptional.isEmpty())
            throw new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody("Auth type not found"), HttpStatus.UNAUTHORIZED);
        if (!authTypeOptional.get().getIsActive())
            throw new ApplicationException(ExceptionEnum.ERR_1038.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        AuthAdaptor adaptor = authTypeOptional.get().getAuthAdapter();

        if (authRequest instanceof LoginOtpDto) {
            if(adaptor.equals(AuthAdaptor.SMS_OTP)) {
                String[] phoneArr = ((LoginOtpDto) authRequest).getPrinciple().split("-");
                user = this.userRepository.findByPhoneNumberAndCountryCode(phoneArr[1], phoneArr[0]).map(UserMapper::toDto);
            } else if(adaptor.equals(AuthAdaptor.EML_OTP)) {
                user = this.userRepository.findByEmail(((LoginOtpDto) authRequest).getPrinciple()).map(UserMapper::toDto);
            }
            if (user.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.UNAUTHORIZED);

        } else {

            userLogin = this.userLoginHistoryService.getUserLoginFromCurrentSessionByAuthType(authType);
            if (ApplicationConstants.ANONYMOUS_USER.equals(userLogin.getUserId()))
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.UNAUTHORIZED);

            user = this.userRepository.findByUserId(userLogin.getUserId()).map(UserMapper::toDto);
            if (user.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.UNAUTHORIZED);

            String principle;
            switch (adaptor) {
                case SMS_OTP -> {
                    principle = user.get().getPhoneNumber();
                }
                case EML_OTP -> {
                    principle = user.get().getEmail();
                }
                default -> throw new ApplicationException(ExceptionEnum.ERR_1025.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            }

            userLogin.setLoginPrinciple(principle);
            this.userLoginHistoryService.updateUserLoginHistoryRecord(userLogin);
        }
        authResponse.setAuthType(authType);
        authResponse.setSecurityKey(this.passwordService.requestOtp(user.get(), adaptor, authTypeOptional.get(), identityProviderDto.getIdentityProviderType().name(), reportingParams.getDeviceId()));
        return SuccessEnum.SUCCESS.getSuccessResponseBody(authResponse,"OTP sent successfully");
    }
}