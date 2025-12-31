package com.photon.identity.authentication.service;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.dto.mapper.UserMapper;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.handler.idp.IDPFactory;
import com.photon.identity.authentication.handler.idp.api.IdentityProvider;
import com.photon.identity.authentication.handler.otphandler.OtpFactory;
import com.photon.identity.authentication.utils.SecurePasswordUtil;
import com.photon.identity.commons.properties.IdentityConfigProperties;
import com.photon.identity.commons.utils.IdentityAlertHandler;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.authentication.dto.request.ActivateUserAuthenticationDto;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.IdentityProviderOperation;
import com.photon.identity.authentication.repository.UserRepository;
import com.photon.identity.idp.utils.IdentityProviderHandler;
import com.photon.properties.AlertEventConfigProperties;
import com.photon.utils.HttpRequestManager;
import com.photon.utils.PhotonUtils;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityProviderHandler identityProviderHandler;
    private final IdentityAlertHandler identityAlertHandler;
    private final IdentityConfigProperties identityConfigProperties;
    private final IDPFactory idpFactory;
    private final OtpFactory otpFactory;
    private final AlertEventConfigProperties alertEventConfigProperties;

    protected IdentityService(UserRepository userRepository, PasswordEncoder passwordEncoder, IdentityProviderHandler identityProviderHandler, IdentityAlertHandler identityAlertHandler, IdentityConfigProperties identityConfigProperties, IDPFactory idpFactory, OtpFactory otpFactory, AlertEventConfigProperties alertEventConfigProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.identityProviderHandler = identityProviderHandler;
        this.identityAlertHandler = identityAlertHandler;
        this.identityConfigProperties = identityConfigProperties;
        this.idpFactory = idpFactory;
        this.otpFactory = otpFactory;
        this.alertEventConfigProperties = alertEventConfigProperties;
    }

    public ApiResponseDto<AuthResponseDto> authenticate(String idp, String authType, IdentityProviderOperation operation, Object authRequest) throws ApplicationException {
        try {
            IdentityProviderDto identityProvider = this.identityProviderHandler.getIdentityProvider(idp);
            return switch (operation){
                case LOGIN -> this.idpFactory.getIdp(identityProvider.getIdentityProviderType()).authenticate(identityProvider, authType, authRequest);
                case REQUEST_OTP -> this.otpFactory.getOtpHandler(identityProvider.getIdentityProviderType()).requestOtp(authType, identityProvider, authRequest);
            };
        } catch (ApplicationException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1016.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public ApiResponseDto<?> logout(String provider) throws ApplicationException {
        try {
            IdentityProviderDto identityProvider = this.identityProviderHandler.getIdentityProvider(provider);
            IdentityProvider idp = this.idpFactory.getIdp(identityProvider.getIdentityProviderType());
            return idp.logOut();
        } catch (ApplicationException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new ApplicationException(ExceptionEnum.ERR_1016.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> activateUser(ActivateUserAuthenticationDto activateUserAuthentication) throws ApplicationException {
        try {
            User user = this.userRepository.findByUserName(activateUserAuthentication.getPrinciple())
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.NOT_FOUND));

            Set<AuthAdaptor> activeAuthAdaptors = Optional.ofNullable(user.getActiveAuthAdapters()).orElse(new HashSet<>());
            activeAuthAdaptors.add(activateUserAuthentication.getAuthAdaptor());
            user.setActiveAuthAdapters(activeAuthAdaptors);
            this.userRepository.save(user);

            return SuccessEnum.SUCCESS.getSuccessResponseBody("User successfully activated for Auth Adaptor: " + activateUserAuthentication.getAuthAdaptor().getName());
        } catch (ApplicationException e) {
            log.error(e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1019.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> changeUserPassword(ActivateUserAuthenticationDto activateUserAuthentication) throws ApplicationException {
        try {
            String userId = HttpRequestManager.getUserId();
            User user = this.userRepository.findByUserId(userId)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.NOT_FOUND));

            Set<AuthAdaptor> activeAuthAdaptors = Optional.of(user.getActiveAuthAdapters().stream().filter(adaptor -> !adaptor.equals(AuthAdaptor.TMP_PWD)).collect(Collectors.toSet())).orElse(new HashSet<>());
            activeAuthAdaptors.add(AuthAdaptor.STATIC_PWD);
            user.setActiveAuthAdapters(activeAuthAdaptors);
            user.setPassword(passwordEncoder.encode(activateUserAuthentication.getCredential()));

            if(alertEventConfigProperties.feature("USER").action("UPDATE_USER_BY_ADMIN").event("success").isEnabled()) {
                UserDto userDto = UserMapper.toDto(user);
                identityAlertHandler.dispatchUserChangePwdSuccessAck(userDto);
            }

            this.userRepository.save(user);
            return SuccessEnum.SUCCESS.getSuccessResponseBody("User successfully activated for Auth Adaptor: STATIC_PWD, with updated password");
        } catch (ApplicationException e) {
            log.error(e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1019.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> requestToSendTempPassword(String userName) throws ApplicationException {
        char[] pwd = null;
        try {
            User user = this.userRepository.findByUserName(userName)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.NOT_FOUND));

            pwd = SecurePasswordUtil.generatePassword(16, true);
            Set<AuthAdaptor> activeAuthAdaptors = Optional.ofNullable(user.getActiveAuthAdapters()).orElse(new HashSet<>());
            activeAuthAdaptors.add(AuthAdaptor.TMP_PWD);
            user.setActiveAuthAdapters(activeAuthAdaptors);
            user.setLastPasswordUpdatedOn(new Date());
            user.setPassword(this.passwordEncoder.encode(new String(pwd)));
            this.userRepository.save(user);

            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("password", new String(pwd));
            placeHolders.put("expiry", PhotonUtils.convertMinutesToFriendlyDuration(this.identityConfigProperties.getTmpPwdExpInMinutes()));
            this.identityAlertHandler.dispatchUserTempPwdForResetPwd(placeHolders, user.getUserId());

            return SuccessEnum.SUCCESS.getSuccessResponseBody("User successfully activated for Auth Adaptor: " + userName + ", with updated password");
        } catch (ApplicationException e) {
            log.error(e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1019.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            SecurePasswordUtil.wipe(pwd);
        }
    }
}