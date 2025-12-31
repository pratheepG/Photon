package com.photon.identity.authentication.handler.loginhandler.impl;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.customtoken.EmailOtpAuthenticationToken;
import com.photon.identity.authentication.dto.UserLoginHistoryDto;
import com.photon.identity.authentication.dto.request.VerifyOtpDto;
import com.photon.identity.authentication.entity.TempCredential;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.handler.loginhandler.api.PhotonAuthenticationProvider;
import com.photon.identity.authentication.repository.TempCredentialRepository;
import com.photon.identity.authentication.repository.UserRepository;
import com.photon.identity.commons.enums.IdentityProviderType;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.EmailAuthConfigDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.utils.CredentialExpiryChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("emailOtpAuthenticationProvider")
public class EmailOtpAuthenticationProvider implements PhotonAuthenticationProvider {
    private final AuthenticationManager authenticationManager;
    private final TempCredentialRepository tempCredentialRepository;
    private final UserRepository userRepository;

    public EmailOtpAuthenticationProvider(AuthenticationManager authenticationManager, TempCredentialRepository tempCredentialRepository, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tempCredentialRepository = tempCredentialRepository;
        this.userRepository = userRepository;
    }

    /**
     * @param identityProvider IdentityProviderDto
     * @param authType AuthTypeDto
     * @param authRequest Object
     * @return Authentication
     * @throws ApplicationException ApplicationException
     */
    @Override
    public Authentication authenticate(IdentityProviderDto identityProvider, AuthTypeDto authType, Object authRequest) throws ApplicationException {

        VerifyOtpDto verifyOtp = (VerifyOtpDto) authRequest;
        Optional<User> userDetails;
        Authentication authentication;

        Optional<TempCredential> tempCredential = this.tempCredentialRepository.findBySecurityKey(verifyOtp.getSecurityKey());
        if (tempCredential.isEmpty())
            throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Credential not found"), HttpStatus.UNAUTHORIZED);

        userDetails = this.userRepository.findByUserId(tempCredential.get().getUserId());
        if (userDetails.isEmpty() && identityProvider.getIdentityProviderType().equals(IdentityProviderType.LOGIN))
            throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("User not found"), HttpStatus.UNAUTHORIZED);

        if(!identityProvider.getId().equalsIgnoreCase(tempCredential.get().getIdp()))
            throw new ApplicationException(ExceptionEnum.ERR_1033.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);

        EmailAuthConfigDto authConfig = (EmailAuthConfigDto) authType.getConfig();
        if(CredentialExpiryChecker.isCredentialExpired(tempCredential.get().getCreatedDate(), authConfig.getOtpExpiryInMinutes()))
            throw new ApplicationException(ExceptionEnum.ERR_1052.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);

        Authentication token = switch (identityProvider.getIdentityProviderType()) {
            case LOGIN -> new EmailOtpAuthenticationToken(userDetails.get(), tempCredential.get(), verifyOtp.getSecurityKey(), verifyOtp.getPassword());
            case ON_BOARDING -> new EmailOtpAuthenticationToken(tempCredential.get(), verifyOtp.getSecurityKey(), verifyOtp.getPassword());
            case GUEST_LOGIN -> null;
        };

        authentication = authenticationManager.authenticate(token);

        if(authentication.isAuthenticated())
            this.tempCredentialRepository.deleteById(tempCredential.get().getId());

        return authentication;
    }
}