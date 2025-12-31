package com.photon.identity.authentication.handler.idp.impl;

import com.photon.auth.enums.StaticRoles;
import com.photon.constants.ApplicationConstants;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.*;
import com.photon.identity.authentication.dto.mapper.UserDeviceMapper;
import com.photon.identity.authentication.dto.mapper.UserMapper;
import com.photon.identity.authentication.entity.RefreshToken;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.entity.UserDevice;
import com.photon.identity.authentication.handler.idp.api.IdentityProvider;
import com.photon.identity.authentication.handler.loginhandler.PhotonAuthenticationProviderFactory;
import com.photon.identity.authentication.repository.UserDeviceRepository;
import com.photon.identity.authentication.service.RefreshTokenService;
import com.photon.identity.authentication.service.UserDeviceService;
import com.photon.identity.authentication.service.UserLoginHistoryService;
import com.photon.identity.authentication.utils.JwtTokenUtil;
import com.photon.identity.authentication.utils.MFAConditionEvaluator;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.LoginFactor;
import com.photon.identity.commons.grpc.client.DeviceSyncService;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.properties.ApplicationConfigProperties;
import com.photon.utils.HttpRequestManager;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("loginIDP")
public class LoginIDP extends IdentityProvider {

    private final DeviceSyncService deviceSyncService;
    private final UserDeviceService userDeviceService;
    private final RefreshTokenService refreshTokenService;
    private final MFAConditionEvaluator mfaConditionEvaluator;
    private final UserLoginHistoryService userLoginHistoryService;
    private final ApplicationConfigProperties applicationConfigProperties;
    private final PhotonAuthenticationProviderFactory photonAuthenticationProviderFactory;

    public LoginIDP(UserDeviceService userDeviceService, RefreshTokenService refreshTokenService,
                    UserDeviceRepository userDeviceRepository, PhotonAuthenticationProviderFactory photonAuthenticationProviderFactory,
                    MFAConditionEvaluator mfaConditionEvaluator, UserLoginHistoryService userLoginHistoryService,
                    DeviceSyncService deviceSyncService, StringRedisTemplate stringRedisTemplate,
                    ApplicationConfigProperties applicationConfigProperties) {
        super(stringRedisTemplate, userDeviceRepository, refreshTokenService);
        this.userDeviceService = userDeviceService;
        this.refreshTokenService = refreshTokenService;
        this.photonAuthenticationProviderFactory = photonAuthenticationProviderFactory;
        this.mfaConditionEvaluator = mfaConditionEvaluator;
        this.userLoginHistoryService = userLoginHistoryService;
        this.deviceSyncService = deviceSyncService;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    @Override
    public ApiResponseDto<AuthResponseDto> authenticate(IdentityProviderDto identityProviderDto, String authType, Object authRequest) throws ApplicationException {
        try {
            ReportingParamsDto reportingParams = HttpRequestManager.getReportingParams();
            UserLoginSessionDto userLoginSession = this.userLoginHistoryService.getCurrentLoginSession(authRequest, authType, identityProviderDto, reportingParams);
            UserLoginHistoryDto userLogin = userLoginSession.getUserLogin();
            AuthAdaptor adaptor = userLogin.getAuthAdaptor();
            UserDto user;
            DeviceInfoDto deviceInfo = DeviceInfoDto.builder().deviceId(reportingParams.getDeviceId()).deviceType(reportingParams.getDeviceType()).build();

            Authentication authentication = this.photonAuthenticationProviderFactory.getAuthenticationProvider(adaptor).authenticate(identityProviderDto, userLogin.getAuthType(), authRequest);

            if (!authentication.isAuthenticated()) {
                this.userLoginHistoryService.saveFailedLogin(userLoginSession.getUserLogin(), "Invalid credentials", userLoginSession.getIdpCode());
                throw new ApplicationException(ExceptionEnum.ERR_1014.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            }

            user = UserMapper.toDto((User) authentication.getPrincipal());

            if (ApplicationConstants.ANONYMOUS_USER.equals(userLogin.getUserId())) {
                this.userLoginHistoryService.updateUserLoginSessionWithUserId(userLoginSession, user, identityProviderDto);
                userLogin = userLoginSession.getUserLogin();
            }

            if (user.getActiveAuthAdapters().contains(AuthAdaptor.TMP_PWD)) {
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.tempLogin(identityProviderDto, (User) authentication.getPrincipal(), userLoginSession));
            }

            boolean isUserMfaEnabled = user.getIsMfaEnabled();
            boolean isIdentityProviderMfaEnabled = identityProviderDto.getIsMfaEnabled();


            if (isIdentityProviderMfaEnabled && !isUserMfaEnabled) {
                throw new ApplicationException(ExceptionEnum.ERR_1018.getErrorResponseBody("Second factor auth is not activated.", this.getRequiredUserSecondFactors(identityProviderDto, (User) authentication.getPrincipal())), HttpStatus.UNAUTHORIZED);
            }

            if (!isUserMfaEnabled) {
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.fullLogin(identityProviderDto, deviceInfo, (User) authentication.getPrincipal(), userLogin.getLoginPrinciple(), authentication, userLoginSession));
            }




            if (identityProviderDto.getIsMfaRequiredForEveryLogin() && userLogin.getLoginFactor().equals(LoginFactor.FIRST_FACTOR_LOGIN) && !userLoginSession.getRequiredSecondFactors().isEmpty())
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.firstFactorLogin(identityProviderDto, authentication, userLoginSession));

            if (identityProviderDto.getIsMfaConditionCheckEnabled() && userLogin.getLoginFactor().equals(LoginFactor.FIRST_FACTOR_LOGIN)
                    && this.evaluateMFAConditions(identityProviderDto, (User) authentication.getPrincipal(), reportingParams, identityProviderDto)
                    && !userLoginSession.getRequiredSecondFactors().isEmpty()){
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.firstFactorLogin(identityProviderDto, authentication, userLoginSession));
            }


            if(userLogin.getLoginFactor().equals(LoginFactor.FIRST_FACTOR_LOGIN))
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.fullLogin(identityProviderDto, deviceInfo, (User) authentication.getPrincipal(), userLogin.getLoginPrinciple(), authentication, userLoginSession));

            if(userLogin.getLoginFactor().equals(LoginFactor.SECOND_FACTOR_LOGIN) && !userLoginSession.getRequiredSecondFactors().isEmpty())
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.firstFactorLogin(identityProviderDto, authentication, userLoginSession));

            else if(userLogin.getLoginFactor().equals(LoginFactor.SECOND_FACTOR_LOGIN))
                return SuccessEnum.SUCCESS.getSuccessResponseBody(this.fullLogin(identityProviderDto, deviceInfo, (User) authentication.getPrincipal(), userLogin.getLoginPrinciple(), authentication, userLoginSession));

        } catch (ApplicationException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InternalAuthenticationServiceException e){
            log.error(e.getMessage(), e);
            throw (ApplicationException) e.getCause();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1016.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }
        throw new ApplicationException(ExceptionEnum.ERR_1016.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param deviceInfo DeviceInfoDto
     * @param user User
     * @param principle String
     * @param authentication Authentication
     * @return AuthResponseDto
     * @throws ApplicationException ApplicationException
     */
    private AuthResponseDto fullLogin(IdentityProviderDto identityProvider, DeviceInfoDto deviceInfo, final User user, String principle, Authentication authentication, UserLoginSessionDto userLoginSession) throws ApplicationException {
        return generateAuthResponse(identityProvider, deviceInfo, user, principle, authentication, userLoginSession);
    }

    /**
     * @param authentication Authentication
     * @return AuthResponseDto
     * @throws ApplicationException ApplicationException
     */
    private AuthResponseDto firstFactorLogin(IdentityProviderDto identityProvider, Authentication authentication, UserLoginSessionDto userLoginSession) throws ApplicationException {

        this.userLoginHistoryService.saveSuccessLogin((User) authentication.getPrincipal(), userLoginSession.getUserLogin(), userLoginSession.getIdpCode());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return AuthResponseDto.builder()
                .expiryDuration(identityProvider.getSessionTimeoutMinutes().longValue())
                .isAuthenticated(true)
                .authType(userLoginSession.getUserLogin().getAuthType().getId())
                .isMfaEnabled(true)
                .idpCode(userLoginSession.getIdpCode())
                .secondFactors(userLoginSession.getRequiredSecondFactors())
                .build();
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param user User
     * @return AuthResponseDto
     * @throws ApplicationException ApplicationException
     */
    private AuthResponseDto tempLogin(IdentityProviderDto identityProvider, final User user, UserLoginSessionDto userLoginSession) throws ApplicationException {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(identityProvider);
        Map<String, Object> claims = new HashMap<>();

        claims.put("user-type", "REGISTERED_USER");
        claims.put("roles", Set.of(new String[]{StaticRoles.ACTIVATE_AUTH.name().concat("|").concat(AccessLevel.OWNER.name())}));

        return AuthResponseDto.builder()
                .isAuthenticated(true)
                .isTempCredential(true)
                .message("Successfully logged-in with temp credential.")
                .authType(userLoginSession.getUserLogin().getAuthType().getId())
                .accessToken(jwtTokenUtil.generateToken(user.getUserId(), claims, userLoginSession.getUserLogin().getAuthType().getId(), userLoginSession.getUserLogin().getAuthAdaptor(), this.applicationConfigProperties))
                .securityAttributes(this.getTempCredSecurityAttributes(user)).userAttributes(this.getUserAttributes(user))
                .expiryDuration(identityProvider.getSessionTimeoutMinutes().longValue())
                .tokenType("JWT").build();
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param deviceInfo DeviceInfoDto
     * @param user User
     * @param principle String
     * @param authentication Authentication
     * @return AuthResponseDto
     */
    @Transactional
    private AuthResponseDto generateAuthResponse(IdentityProviderDto identityProvider, DeviceInfoDto deviceInfo, User user, String principle, Authentication authentication, UserLoginSessionDto userLoginSession) throws ApplicationException {

        Map<String, Object> claims = new HashMap<>();
        Optional<UserDevice> userDeviceOptional = userDeviceService.findByUserId(user.getUserId());
        Optional<UserDevice> deviceOptional = userDeviceService.findByDeviceId(deviceInfo.getDeviceId());

        if (userDeviceOptional.isEmpty() && deviceOptional.isEmpty()) {
            this.userDeviceService.registerDevice();
        }

        if(userDeviceOptional.isEmpty() && deviceOptional.isPresent()) {
            UserDevice device = deviceOptional.get();
            device.setUserId(user.getUserId());
            this.userDeviceService.save(device);
            this.deviceSyncService.linkDeviceToSubscriber(user.getSubscriberId(), deviceInfo.getDeviceId(), user.getUserId());
        }

        if (userDeviceOptional.isPresent() && !Objects.equals(deviceInfo.getDeviceId(), userDeviceOptional.get().getDeviceId())) {

            if (userDeviceOptional.get().getRefreshToken() != null) {
                refreshTokenService.delete(userDeviceOptional.get().getRefreshToken());
            }

            if (userDeviceOptional.get().getUserId() != null) {
                userDeviceOptional.get().setUserId(null);
                userDeviceOptional.get().setIsRefreshActive(false);
                this.userDeviceService.save(userDeviceOptional.get());
            }

            this.deviceSyncService.linkDeviceToSubscriber(user.getSubscriberId(), deviceInfo.getDeviceId(), user.getUserId());
        }

        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(identityProvider);
        RefreshToken refreshToken = jwtTokenUtil.createRefreshToken(identityProvider.getRefreshTokenSessionTimeoutMinutes().longValue());
        UserDevice userDevice = this.userDeviceService.findByDeviceId(deviceInfo.getDeviceId()).orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1029.getErrorResponseBody(), HttpStatus.BAD_REQUEST));

        userDevice.setRefreshToken(refreshToken);
        userDevice.setUserId(user.getUserId());
        userDevice.setIsRefreshActive(true);
        refreshToken.setUserDevice(userDevice);

        refreshToken = this.refreshTokenService.save(refreshToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        claims.put("user-type", "REGISTERED_USER");
        claims.put("roles", user.getRoles().stream().map(role -> role.getRoleId().concat("|").concat(role.getAccessLevel().name())).collect(Collectors.toSet()));
        claims.put("tenant", user.getTenants().stream().map(tenant -> tenant.getTenantId().toString()).collect(Collectors.joining("|")));

        return AuthResponseDto.builder()
                .isAuthenticated(true).isTempCredential(false).message("Successfully logged-in.")
                .authType(userLoginSession.getUserLogin().getAuthType().getId())
                .accessToken(jwtTokenUtil.generateToken(user.getUserId(), claims, userLoginSession.getUserLogin().getAuthType().getId(), userLoginSession.getUserLogin().getAuthAdaptor(), this.applicationConfigProperties))
                .securityAttributes(this.getSecurityAttributes(user)).userAttributes(this.getUserAttributes(user))
                .refreshToken(refreshToken.getToken()).expiryDuration(identityProvider.getSessionTimeoutMinutes().longValue())
                .tokenType("JWT").build();
    }

    /**
     * @param user User
     * @return HashMap<String, Object>
     */
    private HashMap<String, Object> getUserAttributes(User user) {
        HashMap<String, Object> userAttributes = new HashMap<>();

        final String profileStatus = (user.getIsAccountNonLocked() && user.getIsEnabled())?"Active":
                user.getIsAccountNonLocked()?"Locked":"Disabled";

        userAttributes.put("firstName", user.getFirstName());
        userAttributes.put("lastName", user.getLastName());
        userAttributes.put("dateOfBirth", user.getDob());
        userAttributes.put("gender", user.getSex());
        userAttributes.put("phoneNumber", user.getPhoneNumber());
        userAttributes.put("userId", user.getUserId());
        userAttributes.put("profileImg", user.getProfilePic());
        userAttributes.put("profileStatus", profileStatus);
        userAttributes.put("address", user.getAddress());
        return userAttributes;
    }

    /**
     * @param user User
     * @return HashMap<String, Object>
     */
    private HashMap<String, Object> getSecurityAttributes(User user) {
        HashMap<String, Object> securityAttributes = new HashMap<>();

        final String profileStatus = (user.getIsAccountNonLocked() && user.getIsEnabled())?"Active":
                user.getIsAccountNonLocked()?"Locked":"Disabled";

        String userRole = user.getRoles().stream()
                .map(role -> role.getId()+":"+role.getRoleId())
                .collect(Collectors.joining("|"));

        securityAttributes.put("userRole", userRole);
        securityAttributes.put("profileStatus", profileStatus);
        return securityAttributes;
    }

    /**
     * @param user User
     * @return HashMap<String, Object>
     */
    private HashMap<String, Object> getTempCredSecurityAttributes(User user) {
        HashMap<String, Object> securityAttributes = new HashMap<>();

        final String profileStatus = (user.getIsAccountNonLocked() && user.getIsEnabled())?"Active": user.getIsAccountNonLocked()?"Locked":"Disabled";

        String userRole = "*:"+StaticRoles.ACTIVATE_AUTH.name();

        securityAttributes.put("userRole", userRole);
        securityAttributes.put("profileStatus", profileStatus);
        return securityAttributes;
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param user User
     * @return Map<String, Set<String>>
     */
    private Map<String, Set<String>> getRequiredUserSecondFactors(IdentityProviderDto identityProvider, User user) {
        Map<String, Set<String>> requiredUserSecondFactors = new HashMap<>();

        Set<String> requiredMfa = identityProvider.getIdentityProviderAuthTypes()
                .stream().flatMap(identityProviderAuthTypeDto -> identityProviderAuthTypeDto.getSecondFactors().stream())
                .map(AuthTypeDto::getId).collect(Collectors.toSet());

        Set<String> activeMfa = user.getActiveAuthAdapters()
                .stream().map(AuthAdaptor::getName).filter(requiredMfa::contains).collect(Collectors.toSet());

        requiredUserSecondFactors.put("activeMfa", activeMfa);
        requiredUserSecondFactors.put("requiredMfa", requiredMfa);

        return requiredUserSecondFactors;
    }

    /**
     * Evaluate MFA conditions for a given IdentityProvider and LoginRequest.
     * @param identityProvider  IdentityProviderDto Object
     * @param user     The user Object
     * @return True if MFA should be triggered, false otherwise
     */
    private boolean evaluateMFAConditions(IdentityProviderDto identityProvider, User user, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto) {
        UserDto userDto = UserMapper.toDto(user);
        UserDeviceDto userDeviceDto = userDeviceService.findByUserId(user.getUserId()).map(UserDeviceMapper::toDto).orElse(null);
        return mfaConditionEvaluator.evaluate(identityProvider.getMfaCondition(), userDto, userDeviceDto, reportingParamsDto, identityProviderDto);
    }

}