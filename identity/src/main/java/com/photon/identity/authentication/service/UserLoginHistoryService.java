package com.photon.identity.authentication.service;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.photon.constants.ApplicationConstants;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.dto.UserLoginHistoryDto;
import com.photon.identity.authentication.dto.UserLoginSessionDto;
import com.photon.identity.authentication.dto.request.LoginStaticPasswordDto;
import com.photon.identity.authentication.dto.request.VerifyOtpDto;
import com.photon.identity.authentication.entity.TempCredential;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.repository.TempCredentialRepository;
import com.photon.identity.authentication.utils.UniqueCodeGenerator;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.LoginStatus;
import com.photon.identity.commons.enums.LoginFactor;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderAuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.utils.HttpRequestManager;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginHistoryService {

    private final TempCredentialRepository tempCredentialRepository;
    private final HttpSession httpSession;
    private static final String IDP_CODE = "IdpCode";

    /**
     * @param user User
     * @param userLogin UserLoginHistoryDto
     */
    public void saveSuccessLogin(User user, UserLoginHistoryDto userLogin, String idpCode) {
        if (!user.getUserId().equals(userLogin.getUserId())) {
            throw new ApplicationException(ExceptionEnum.ERR_1022.getErrorResponseBody(), HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }

        Set<UserLoginHistoryDto> userLoginHistoryList = getUserLoginHistoryList(idpCode);

        userLoginHistoryList.forEach(userLoginHistory -> {
            if (Objects.equals(userLoginHistory.getSessionId(), userLogin.getSessionId())
                    && Objects.equals(userLoginHistory.getAuthType().getId(), userLogin.getAuthType().getId())
                    && Objects.equals(userLoginHistory.getLoginStatus(), LoginStatus.INITIATED)) {

                userLoginHistory.setIsSuccess(true);
                userLoginHistory.setLoginStatus(LoginStatus.SUCCESS);
                userLoginHistory.setLoginTimestamp(new Date());
                userLoginHistory.setFailureReason("");
            }
        });

        saveUserLoginHistoryList(userLoginHistoryList, idpCode);
    }

    @Async
    public void saveFailedLogin(UserLoginHistoryDto userLogin, String failureReason, String idpCode) {

        userLogin.setIsSuccess(false);
        userLogin.setLoginStatus(LoginStatus.FAILED);
        userLogin.setLoginTimestamp(new Date());
        userLogin.setFailureReason(failureReason);

        Set<UserLoginHistoryDto> userLoginHistoryList = getUserLoginHistoryList(idpCode);
        userLoginHistoryList.forEach(userLoginHistory->{
            if (Objects.equals(userLoginHistory.getSessionId(), userLogin.getSessionId())
                    && Objects.equals(userLoginHistory.getAuthType().getId(), userLogin.getAuthType().getId())
                    && Objects.equals(userLoginHistory.getLoginStatus(), LoginStatus.INITIATED)) {
                userLoginHistoryList.add(userLogin);
            }
        });
        saveUserLoginHistoryList(userLoginHistoryList, idpCode);
    }

    /**
     * @param authReqObject Object
     * @param authType String
     * @param identityProviderDto IdentityProviderDto
     * @param reportingParams ReportingParamsDto
     * @return UserLoginSessionDto
     */
    public UserLoginSessionDto getCurrentLoginSession(Object authReqObject, String authType, IdentityProviderDto identityProviderDto, ReportingParamsDto reportingParams) {

        if(Objects.isNull(HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE)) || Objects.isNull(httpSession.getAttribute(HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE))))
            return this.initiateNewLoginSession(authReqObject, identityProviderDto, reportingParams, authType);

        AtomicReference<UserLoginSessionDto> userLoginSession = new AtomicReference<>(new UserLoginSessionDto());
        this.getUserLoginHistoryList(HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE))
                .forEach(userLoginHistoryDto -> {
                    if(userLoginHistoryDto.getLoginStatus().equals(LoginStatus.INITIATED)) {
                        if (userLoginHistoryDto.getAuthType().getId().equals(authType)) {
                            userLoginHistoryDto.setLoginTimestamp(new Date());
                            userLoginSession.get().setUserLogin(userLoginHistoryDto);
                        }

                        if(Objects.equals(userLoginHistoryDto.getLoginFactor(), LoginFactor.SECOND_FACTOR_LOGIN) && !userLoginHistoryDto.getAuthType().getId().equals(authType)) {
                            Set<String> reqSF = userLoginSession.get().getRequiredSecondFactors();
                            reqSF.add(userLoginHistoryDto.getAuthType().getId());
                            userLoginSession.get().setRequiredSecondFactors(reqSF);
                        }
                    } else if (userLoginHistoryDto.getLoginStatus().equals(LoginStatus.SUCCESS)
                            && Objects.equals(userLoginHistoryDto.getLoginFactor(), LoginFactor.FIRST_FACTOR_LOGIN)
                            && userLoginHistoryDto.getAuthType().getId().equals(authType)) {
                        this.clearLoginSession();
                        userLoginSession.set(this.initiateNewLoginSession(authReqObject, identityProviderDto, reportingParams, authType));
                    }
                });

        if (Objects.isNull(userLoginSession.get().getUserLogin())) {
            return this.initiateNewLoginSession(authReqObject, identityProviderDto, reportingParams, authType);
        }

        userLoginSession.get().setIdpCode(HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE));
        return userLoginSession.get();
    }

    /**
     * @param authReqObject Object
     * @param identityProviderDto IdentityProviderDto
     * @param reportingParams ReportingParamsDto
     * @param authType String
     * @return UserLoginSessionDto
     */
    private UserLoginSessionDto initiateNewLoginSession(Object authReqObject, IdentityProviderDto identityProviderDto, ReportingParamsDto reportingParams, String authType){
        final String idpCode = UniqueCodeGenerator.generateCodeWithChar(4);
        UserLoginSessionDto userLoginSession = new UserLoginSessionDto();
        IdentityProviderAuthTypeDto identityProviderAuthTypes = identityProviderDto.getIdentityProviderAuthTypes()
                .stream().filter(o -> o.getFirstFactor().getId().equals(authType)).findFirst()
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody(), HttpStatus.BAD_REQUEST));

        if (!identityProviderAuthTypes.getFirstFactor().getIsActive())
            throw new ApplicationException(ExceptionEnum.ERR_1038.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);

        String principle = this.getPrinciple(authReqObject, identityProviderAuthTypes.getFirstFactor().getAuthAdapter());
        UserLoginHistoryDto userLoginHistoryDto = this.generateUserLogin(principle, ApplicationConstants.ANONYMOUS_USER, identityProviderAuthTypes.getFirstFactor(), reportingParams, LoginFactor.FIRST_FACTOR_LOGIN, LoginStatus.INITIATED, new Date(), new Date());
        Set<UserLoginHistoryDto> userLoginHistorySet = new HashSet<>();
        userLoginHistorySet.add(userLoginHistoryDto);
        userLoginSession.setUserLogin(userLoginHistoryDto);

        identityProviderAuthTypes.getSecondFactors().forEach(authTypeDto -> {
            Set<String> reqSF = userLoginSession.getRequiredSecondFactors();
            reqSF.add(authTypeDto.getId());
            userLoginSession.setRequiredSecondFactors(reqSF);
            userLoginHistorySet.add(this.generateUserLogin(null, ApplicationConstants.ANONYMOUS_USER, authTypeDto, reportingParams, LoginFactor.SECOND_FACTOR_LOGIN, LoginStatus.INITIATED, null, new Date()));
        });
        this.saveUserLoginHistoryList(userLoginHistorySet, idpCode);

        userLoginSession.setIdpCode(idpCode);
        return userLoginSession;
    }

    public void updateUserLoginSessionWithUserId(UserLoginSessionDto userLoginSession, UserDto user, IdentityProviderDto identityProvider) {

        Set<UserLoginHistoryDto> loginHistoryList = getUserLoginHistoryList(userLoginSession.getIdpCode());
        Set<UserLoginHistoryDto> userLoginHistoryList = new HashSet<>();

        loginHistoryList.forEach(userLoginHistory -> {
            userLoginHistory.setUserId(user.getUserId());
            userLoginHistoryList.add(userLoginHistory);

            if(userLoginSession.getUserLogin().getAuthType().getId().equals(userLoginHistory.getAuthType().getId())) {
                userLoginSession.setUserLogin(userLoginHistory);
                userLoginSession.setRequiredSecondFactors(userLoginSession.getRequiredSecondFactors());
            }
        });

        this.clearLoginSession();
        this.saveUserLoginHistoryList(userLoginHistoryList, userLoginSession.getIdpCode());
    }

    public UserLoginHistoryDto getUserLoginFromCurrentSessionByAuthType(String authType){
        String idpCode = HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE);
        return this.getUserLoginHistoryList(idpCode).stream().filter(userLoginHistoryDto -> userLoginHistoryDto.getAuthType().getId().equals(authType))
                .findFirst().orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1022.getErrorResponseBody(), HttpStatus.NON_AUTHORITATIVE_INFORMATION));
    }

    private String getDeviceInfo(ReportingParamsDto reportingParams) {
        JsonObject deviceInfo = new JsonObject();
        deviceInfo.addProperty("deviceId", reportingParams.getDeviceId());
        deviceInfo.addProperty("deviceName", reportingParams.getDeviceName());
        deviceInfo.addProperty("deviceOs", reportingParams.getDeviceOs());
        deviceInfo.addProperty("osVersion", reportingParams.getOsVersion());
        return deviceInfo.toString();
    }

    public void saveUserLoginHistoryList(Set<UserLoginHistoryDto> userLoginHistorySet, String idpCode) {
        try {
            httpSession.setAttribute(idpCode, userLoginHistorySet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize data to JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<UserLoginHistoryDto> getUserLoginHistoryList(String idpCode) {
        try {
            return (Set<UserLoginHistoryDto>) httpSession.getAttribute(idpCode);
        } catch (Exception e) {
            throw new ApplicationException(ExceptionEnum.ERR_1024.getErrorResponseBody(), HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
    }

    /**
     * @param updatedRecord UserLoginHistoryDto
     */
    public void updateUserLoginHistoryRecord(UserLoginHistoryDto updatedRecord) {
        String sessionId = httpSession.getId();
        String idpCode = HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE);

        Set<UserLoginHistoryDto> userLoginHistoryList = getUserLoginHistoryList(idpCode);
        userLoginHistoryList.forEach(userLoginHistory->{
            if (Objects.equals(userLoginHistory.getSessionId(), sessionId)
                    && Objects.equals(userLoginHistory.getAuthType().getId(), updatedRecord.getAuthType().getId())
                    && Objects.equals(userLoginHistory.getLoginStatus(), LoginStatus.INITIATED)) {
                userLoginHistoryList.add(updatedRecord);
            }
        });
        saveUserLoginHistoryList(userLoginHistoryList, idpCode);
    }


    public UserLoginHistoryDto generateUserLogin(String principle, String userId, AuthTypeDto AuthType, ReportingParamsDto reportingParams, LoginFactor loginType, LoginStatus loginStatus, Date loginTimeStamp, Date createdOn ) {
        UserLoginHistoryDto userLoginHistoryDto = new UserLoginHistoryDto();

        if(StringUtils.isNotBlank(principle))
            userLoginHistoryDto.setLoginPrinciple(principle);

        if(StringUtils.isNotBlank(userId))
            userLoginHistoryDto.setUserId(userId);

        userLoginHistoryDto.setSessionId(httpSession.getId());
        userLoginHistoryDto.setLoginTimestamp(loginTimeStamp);
        userLoginHistoryDto.setLoginFactor(loginType);
        userLoginHistoryDto.setLoginStatus(loginStatus);
        userLoginHistoryDto.setUserAgent(reportingParams.getUserAgent());
        userLoginHistoryDto.setAuthType(AuthType);
        userLoginHistoryDto.setAuthAdaptor(AuthType.getAuthAdapter());
        userLoginHistoryDto.setDeviceInfo(this.getDeviceInfo(reportingParams));
        userLoginHistoryDto.setDeviceId(reportingParams.getDeviceId());
        userLoginHistoryDto.setIpAddress(reportingParams.getIP());
        userLoginHistoryDto.setCreatedOn(createdOn);

        return userLoginHistoryDto;
    }


    public void clearLoginSession(){
        httpSession.removeAttribute(HttpRequestManager.getCurrentHttpRequest().getHeader(IDP_CODE));
    }

    private String getPrinciple(Object authRequest, AuthAdaptor authAdaptor){
        String principle;
        switch (authAdaptor) {
            case SMS_OTP -> {
                Optional<TempCredential> tempCredential = this.tempCredentialRepository.findBySecurityKey(((VerifyOtpDto) authRequest).getSecurityKey());
                if(tempCredential.isEmpty())
                    throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
                principle = tempCredential.get().getPhoneNumber();
            }
            case EML_OTP -> {
                Optional<TempCredential> tempCredential = this.tempCredentialRepository.findBySecurityKey(((VerifyOtpDto) authRequest).getSecurityKey());
                if(tempCredential.isEmpty())
                    throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
                principle = tempCredential.get().getEmail();
            }
            case STATIC_PWD -> {
                LoginStaticPasswordDto staticPwdAuthReq = (LoginStaticPasswordDto) authRequest;
                principle = staticPwdAuthReq.getUserName();
            }
            default -> throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }
        return principle;
    }

    private Set<String> getRequiredUserSecondFactors(IdentityProviderDto identityProvider, UserDto user) {
        return identityProvider.getIdentityProviderAuthTypes()
                .stream().flatMap(identityProviderAuthTypeDto -> identityProviderAuthTypeDto.getSecondFactors().stream().filter(AuthTypeDto::getIsActive))
                .filter(authTypeDto -> user.getActiveAuthAdapters().contains(authTypeDto.getAuthAdapter()))
                .map(AuthTypeDto::getId).collect(Collectors.toSet());
    }
}