package com.photon.identity.authentication.handler.idp.impl;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.AuthResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.entity.Role;
import com.photon.identity.authentication.entity.UserDevice;
import com.photon.identity.authentication.handler.idp.api.IdentityProvider;
import com.photon.identity.authentication.handler.loginhandler.PhotonAuthenticationProviderFactory;
import com.photon.identity.authentication.repository.RoleRepository;
import com.photon.identity.authentication.repository.UserDeviceRepository;
import com.photon.identity.authentication.service.RefreshTokenService;
import com.photon.identity.authentication.service.UserDeviceService;
import com.photon.identity.authentication.utils.JwtTokenUtil;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderAuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.properties.ApplicationConfigProperties;
import com.photon.utils.HttpRequestManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("onBoardingIDP")
public class OnBoardingIDP extends IdentityProvider {

    private final UserDeviceService userDeviceService;
    private final RoleRepository roleRepository;
    private final PhotonAuthenticationProviderFactory photonAuthenticationProviderFactory;
    private final ApplicationConfigProperties applicationConfigProperties;

    public OnBoardingIDP(UserDeviceService userDeviceService, StringRedisTemplate stringRedisTemplate,
                         RoleRepository roleRepository, UserDeviceRepository userDeviceRepository,
                         PhotonAuthenticationProviderFactory photonAuthenticationProviderFactory, RefreshTokenService refreshTokenService,
                         ApplicationConfigProperties applicationConfigProperties) {
        super(stringRedisTemplate, userDeviceRepository, refreshTokenService);
        this.userDeviceService = userDeviceService;
        this.roleRepository = roleRepository;
        this.photonAuthenticationProviderFactory = photonAuthenticationProviderFactory;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    /**
     * @param identityProviderDto IdentityProviderDto
     * @param authType String
     * @param authRequest Object
     * @return ApiResponseDto<AuthResponseDto>
     * @throws ApplicationException ApplicationException
     */
    @Override
    public ApiResponseDto<AuthResponseDto> authenticate(IdentityProviderDto identityProviderDto, String authType, Object authRequest) throws ApplicationException {
        AuthTypeDto idpAuthType = identityProviderDto.getIdentityProviderAuthTypes().stream()
                .filter(authTypeDto -> {
                    if(authTypeDto.getFirstFactor() != null && authTypeDto.getFirstFactor().getId().equalsIgnoreCase(authType)) {
                        if (!authTypeDto.getFirstFactor().getIsActive())
                            throw new ApplicationException(ExceptionEnum.ERR_1038.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(IdentityProviderAuthTypeDto::getFirstFactor).findFirst()
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody(), HttpStatus.BAD_REQUEST));

        AuthAdaptor adaptor = idpAuthType.getAuthAdapter();
        Authentication authentication = this.photonAuthenticationProviderFactory.getAuthenticationProvider(adaptor).authenticate(identityProviderDto, idpAuthType, authRequest);

        if (!authentication.isAuthenticated())
            throw new ApplicationException(ExceptionEnum.ERR_1014.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);

        return SuccessEnum.SUCCESS.getSuccessResponseBody(this.generateAuthResponse(identityProviderDto, authType, adaptor, authentication));
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param authentication   Authentication
     * @return AuthResponseDto
     */
    @Transactional
    private AuthResponseDto generateAuthResponse(IdentityProviderDto identityProvider, String authType, AuthAdaptor adaptor, Authentication authentication) throws ApplicationException {
        Map<String, Object> claims = new HashMap<>();
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(identityProvider);

        UserDevice userDevice = this.userDeviceService.findByDeviceId(HttpRequestManager.getReportingParams().getDeviceId())
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1029.getErrorResponseBody(), HttpStatus.BAD_REQUEST));

        userDevice.setRefreshToken(null);
        userDevice.setUserId(null);
        userDevice.setIsRefreshActive(true);

        Optional<Role> role = this.roleRepository.findById(identityProvider.getIdpDefaultRole());

        if(role.isEmpty())
            throw new ApplicationException(ExceptionEnum.ERR_1031.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        claims.put("user-type", "PROSPECT_USER");
        claims.put("roles", role.stream().map(r -> r.getRoleId().concat("|").concat(r.getAccessLevel().name())).collect(Collectors.toSet()));

        return AuthResponseDto.builder()
                .isAuthenticated(true).authType(authType)
                .accessToken(jwtTokenUtil.generateToken( String.valueOf(authentication.getPrincipal()), claims, authType, adaptor, this.applicationConfigProperties))
                .expiryDuration(identityProvider.getSessionTimeoutMinutes().longValue()).tokenType("JWT").build();
    }
}