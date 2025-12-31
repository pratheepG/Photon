package com.photon.identity.authentication.handler.loginhandler.impl;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.customtoken.StaticPasswordAuthenticationToken;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.repository.UserRepository;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.properties.IdentityConfigProperties;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.authentication.dto.request.LoginStaticPasswordDto;
import com.photon.identity.authentication.handler.loginhandler.api.PhotonAuthenticationProvider;
import com.photon.identity.idp.dto.StaticPasswordAuthConfigDto;
import com.photon.utils.CredentialExpiryChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Slf4j
@Component("staticPasswordAuthenticationProvider")
public class StaticPasswordAuthenticationProvider implements PhotonAuthenticationProvider {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final IdentityConfigProperties identityConfigProperties;

    public StaticPasswordAuthenticationProvider(AuthenticationManager authenticationManager, UserRepository userRepository, IdentityConfigProperties identityConfigProperties) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.identityConfigProperties = identityConfigProperties;
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param authType AuthTypeDto
     * @param authRequest Object
     * @return Authentication
     */
    @Override
    public Authentication authenticate(IdentityProviderDto identityProvider, AuthTypeDto authType, Object authRequest) throws ApplicationException {
        LoginStaticPasswordDto staticPasswordAuthRequestDto = (LoginStaticPasswordDto) authRequest;

        User userDetails = this.userRepository.findByUserName(staticPasswordAuthRequestDto.getUserName())
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.UNAUTHORIZED));

        if (userDetails.getActiveAuthAdapters().contains(AuthAdaptor.TMP_PWD)){
            if(CredentialExpiryChecker.isCredentialExpired(userDetails.getLastPasswordUpdatedOn(), this.identityConfigProperties.getTmpPwdExpInMinutes()))
                throw new ApplicationException(ExceptionEnum.ERR_1052.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        } else {
            StaticPasswordAuthConfigDto authConfig = (StaticPasswordAuthConfigDto) authType.getConfig();
            if(CredentialExpiryChecker.isCredentialExpired(userDetails.getLastPasswordUpdatedOn(), authConfig.getPasswordExpiryInMinutes()))
                throw new ApplicationException(ExceptionEnum.ERR_1052.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }

        Authentication token = new StaticPasswordAuthenticationToken(userDetails, staticPasswordAuthRequestDto.getPassword());
        return this.authenticationManager.authenticate(token);
    }
}