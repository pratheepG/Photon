package com.photon.identity.authentication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.alerts.AlertProducer;
import com.photon.alerts.dto.AlertEventDto;
import com.photon.alerts.enums.AlertAudience;
import com.photon.alerts.enums.Channel;
import com.photon.alerts.enums.Priority;
import com.photon.dto.AuthResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.entity.TempCredential;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.CredentialType;
import com.photon.identity.authentication.repository.TempCredentialRepository;
import com.photon.identity.authentication.utils.UniqueCodeGenerator;
import com.photon.identity.idp.dto.EmailAuthConfigDto;
import com.photon.identity.idp.dto.SmsAuthConfigDto;
import com.photon.utils.CredentialExpiryChecker;
import com.photon.utils.HttpRequestManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PasswordService {

    private final AlertProducer alerts;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final TempCredentialService tempCredentialService;
    private final TempCredentialRepository tempCredentialRepository;
    private final HttpSession httpSession;

    public PasswordService(AlertProducer alerts, TempCredentialRepository tempCredentialRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, TempCredentialService tempCredentialService, HttpSession httpSession) {
        this.alerts = alerts;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.tempCredentialService = tempCredentialService;
        this.tempCredentialRepository = tempCredentialRepository;
        this.httpSession = httpSession;
    }

    @Transactional
    public String requestOtp(UserDto user, AuthAdaptor authAdaptor, AuthTypeDto authType, String operation, String deviceId) throws ApplicationException {
        try {
            final String securityKey = UniqueCodeGenerator.generateSecureKey();
            final String requestId = this.httpSession.getId();
            final String idp = HttpRequestManager.getCurrentHttpRequest().getHeader("Provider");
            long maxOtpReq;
            long otpExpMins;
            long otpLength;
            long maxAttemptResetAfterMinutes = 0;
            long resendOtpInSec = 0;
            double resendOtpInSeconds = 0;

            if(authAdaptor.equals(AuthAdaptor.EML_OTP)){
                EmailAuthConfigDto emailAuthConfig = this.objectMapper.convertValue(authType.getConfig(),  EmailAuthConfigDto.class);
                maxOtpReq = emailAuthConfig.getMaxOtpAttempts();
                otpExpMins = emailAuthConfig.getOtpExpiryInMinutes();
                otpLength = emailAuthConfig.getOtpLength();
                resendOtpInSeconds = emailAuthConfig.getResendOtpInSeconds()/60.0;
                resendOtpInSec = emailAuthConfig.getResendOtpInSeconds();
                maxAttemptResetAfterMinutes = emailAuthConfig.getMaxAttemptResetAfterMinutes();
            } else if(authAdaptor.equals(AuthAdaptor.SMS_OTP)) {
                SmsAuthConfigDto smsAuthConfig = this.objectMapper.convertValue(authType.getConfig(),  SmsAuthConfigDto.class);
                maxOtpReq = smsAuthConfig.getMaxOtpAttempts();
                otpExpMins = smsAuthConfig.getOtpExpiryInMinutes();
                otpLength = smsAuthConfig.getOtpLength();
                resendOtpInSeconds = smsAuthConfig.getResendOtpInSeconds()/60.0;
                resendOtpInSec = smsAuthConfig.getResendOtpInSeconds();
                maxAttemptResetAfterMinutes = smsAuthConfig.getMaxAttemptResetAfterMinutes();
            } else {
                otpLength = 4;
                maxOtpReq = 0;
                otpExpMins = 0;
            }

            Date now = new Date();
            Date fromDate = Date.from(now.toInstant().minus(Duration.ofMinutes(maxAttemptResetAfterMinutes)));

            Optional<List<TempCredential>> existingCredentials = this.tempCredentialRepository.findByAuthTypeAndAuthAdaptorAndOperationAndRequestIdAndDeviceIdAndCreatedDateAfter(authType.getId(), authAdaptor, operation, requestId, deviceId, fromDate);
            List<TempCredential> expiredCredentials = existingCredentials.orElse(Collections.emptyList())
                    .stream().filter(O -> CredentialExpiryChecker.isCredentialExpired(O.getCreatedDate(), otpExpMins))
                    .toList();
            List<TempCredential> aliveCredentials = existingCredentials.orElse(Collections.emptyList())
                    .stream().filter(O -> !CredentialExpiryChecker.isCredentialExpired(O.getCreatedDate(), otpExpMins))
                    .sorted(Comparator.comparing(TempCredential::getCreatedDate))
                    .toList();

            if (!expiredCredentials.isEmpty()) {
                this.tempCredentialRepository.deleteAllById(expiredCredentials.stream().map(TempCredential::getId).collect(Collectors.toList()));
            }

            if(!aliveCredentials.isEmpty() && !CredentialExpiryChecker.isCredentialExpired(aliveCredentials.get(0).getCreatedDate(), resendOtpInSeconds)) {
                long remainingSeconds = resendOtpInSec - (Instant.now().getEpochSecond()%60);
                throw new ApplicationException(ExceptionEnum.ERR_1023.getErrorResponseBody("Re-Send is available after " + remainingSeconds + " seconds"), HttpStatus.UNAUTHORIZED);
            }

            if(aliveCredentials.size() >= maxOtpReq){
                long expiryInMillis = otpExpMins * 60 * 1000L;
                long expirationTime = (((aliveCredentials.get(0).getCreatedDate().getTime() + expiryInMillis) - System.currentTimeMillis())/1000)/60;
                throw new ApplicationException(ExceptionEnum.ERR_1023.getErrorResponseBody("Please Try after "+expirationTime+" minutes"), HttpStatus.UNAUTHORIZED);
            }

            final String otp = UniqueCodeGenerator.generateNumericCode((int) otpLength);

            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("otp", otp);
            placeHolders.put("fullName", user.getFirstName().concat(" ").concat(user.getLastName()));
            placeHolders.put("expiryInMinutes", String.valueOf(otpExpMins));

            String alertSubType = "LOGIN_OTP";
            Channel channel = (authAdaptor == AuthAdaptor.SMS_OTP)? Channel.SMS : Channel.E_MAIL;
            AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                    .alertType("AUTHENTICATION")
                    .alertSubType(alertSubType)
                    .placeholders(placeHolders)
                    .isPassword(true).status("SUCCESS")
                    .userId(user.getUserId()).channel(channel)
                    .alertAudience(AlertAudience.INDIVIDUAL)
                    .build();

            this.alerts.sendAlert(Priority.HIGH, alertEventDtoDto);

            TempCredential tempCredential = TempCredential.builder()
                    .idp(idp).credentialType(CredentialType.OTP).retryCount(0).securityKey(securityKey)
                    .id(this.passwordEncoder.encode(otp)).userId(user.getUserId()).deviceId(deviceId)
                    .authAdaptor(authAdaptor).authType(authType.getId()).operation(operation)
                    .email(user.getPrimaryEmailAddress()).phoneNumber(user.getPrimaryPhoneNumber())
                    .userName(user.getUserName()).requestId(requestId).build();

            this.tempCredentialService.createAndInvalidateOld(tempCredential);

            return securityKey;
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public String requestOtp(String principle, AuthAdaptor authAdaptor, AuthTypeDto authType, String operation, String deviceId) throws ApplicationException {
        try {
            final String securityKey = UniqueCodeGenerator.generateSecureKey();
            final String requestId = this.httpSession.getId();
            final String idp = HttpRequestManager.getCurrentHttpRequest().getHeader("Provider");
            long maxOtpReq;
            long otpExpMins;
            long otpLength;
            double resendOtpInSeconds = 0;
            long resendOtpInSec = 0;
            long maxAttemptResetAfterMinutes = 0;

            if(authAdaptor.equals(AuthAdaptor.EML_OTP)){
                EmailAuthConfigDto emailAuthConfig = this.objectMapper.convertValue(authType.getConfig(),  EmailAuthConfigDto.class);
                maxOtpReq = emailAuthConfig.getMaxOtpAttempts();
                otpExpMins = emailAuthConfig.getOtpExpiryInMinutes();
                otpLength = emailAuthConfig.getOtpLength();
                resendOtpInSeconds = emailAuthConfig.getResendOtpInSeconds()/60.0;
                resendOtpInSec = emailAuthConfig.getResendOtpInSeconds();
                maxAttemptResetAfterMinutes = emailAuthConfig.getMaxAttemptResetAfterMinutes();
            } else if(authAdaptor.equals(AuthAdaptor.SMS_OTP)) {
                SmsAuthConfigDto smsAuthConfig = this.objectMapper.convertValue(authType.getConfig(),  SmsAuthConfigDto.class);
                maxOtpReq = smsAuthConfig.getMaxOtpAttempts();
                otpExpMins = smsAuthConfig.getOtpExpiryInMinutes();
                otpLength = smsAuthConfig.getOtpLength();
                resendOtpInSeconds = smsAuthConfig.getResendOtpInSeconds()/60.0;
                resendOtpInSec = smsAuthConfig.getResendOtpInSeconds();
                maxAttemptResetAfterMinutes = smsAuthConfig.getMaxAttemptResetAfterMinutes();
            } else {
                otpLength = 4;
                maxOtpReq = 0;
                otpExpMins = 0;
            }

            Date now = new Date();
            Date fromDate = Date.from(now.toInstant().minus(Duration.ofMinutes(maxAttemptResetAfterMinutes)));

            Optional<List<TempCredential>> existingCredentials = this.tempCredentialRepository.findByAuthTypeAndAuthAdaptorAndOperationAndRequestIdAndDeviceIdAndCreatedDateAfter(authType.getId(), authAdaptor, operation, requestId, deviceId, fromDate);
            List<TempCredential> expiredCredentials = existingCredentials.orElse(Collections.emptyList())
                    .stream().filter(O -> CredentialExpiryChecker.isCredentialExpired(O.getCreatedDate(), otpExpMins))
                    .toList();
            List<TempCredential> aliveCredentials = existingCredentials.orElse(Collections.emptyList())
                    .stream().filter(O -> !CredentialExpiryChecker.isCredentialExpired(O.getCreatedDate(), otpExpMins))
                    .sorted(Comparator.comparing(TempCredential::getCreatedDate))
                    .toList();

            if (!expiredCredentials.isEmpty()) {
                this.tempCredentialRepository.deleteAllById(expiredCredentials.stream().map(TempCredential::getId).collect(Collectors.toList()));
            }

            if(!aliveCredentials.isEmpty() && !CredentialExpiryChecker.isCredentialExpired(aliveCredentials.get(0).getCreatedDate(), resendOtpInSeconds)) {
                long remainingSeconds = resendOtpInSec - (Instant.now().getEpochSecond()%60);
                throw new ApplicationException(ExceptionEnum.ERR_1023.getErrorResponseBody("Re-Send is available after " + remainingSeconds + " seconds"), HttpStatus.UNAUTHORIZED);
            }

            if(aliveCredentials.size() >= maxOtpReq){
                long expiryInMillis = otpExpMins * 60 * 1000L;
                long expirationTime = (((aliveCredentials.get(0).getCreatedDate().getTime() + expiryInMillis) - System.currentTimeMillis())/1000)/60;
                throw new ApplicationException(ExceptionEnum.ERR_1023.getErrorResponseBody("Please Try after "+expirationTime+" minutes"), HttpStatus.UNAUTHORIZED);
            }

            final String otp = UniqueCodeGenerator.generateNumericCode((int) otpLength);

            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("otp", otp);
            placeHolders.put("expiryInMinutes", String.valueOf(otpExpMins));

            String alertSubType = "LOGIN_OTP";
            String phoneNum = (authAdaptor == AuthAdaptor.SMS_OTP)? principle.split("-")[1]: "";
            String countryCode = (authAdaptor == AuthAdaptor.SMS_OTP)? principle.split("-")[0]: "";
            String email = (authAdaptor == AuthAdaptor.EML_OTP)? principle: "";
            Channel channel = (authAdaptor == AuthAdaptor.SMS_OTP)? Channel.SMS : Channel.E_MAIL;
            AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                    .alertType("ONBOARDING")
                    .alertSubType(alertSubType)
                    .placeholders(placeHolders)
                    .phoneNumber(phoneNum)
                    .countryCode(countryCode)
                    .email(email)
                    .isPreLogin(true)
                    .isPassword(true)
                    .status("SUCCESS")
                    .channel(channel)
                    .alertAudience(AlertAudience.INDIVIDUAL)
                    .build();

            this.alerts.sendAlert(Priority.HIGH, alertEventDtoDto);

            TempCredential tempCredential = TempCredential.builder()
                    .idp(idp)
                    .phoneNumber((authAdaptor == AuthAdaptor.SMS_OTP)?principle:null).credentialType(CredentialType.OTP)
                    .retryCount(0).securityKey(securityKey).id(this.passwordEncoder.encode(otp)).deviceId(deviceId)
                    .authAdaptor(authAdaptor).authType(authType.getId()).operation(operation)
                    .email((authAdaptor == AuthAdaptor.EML_OTP)?principle:null).requestId(requestId)
                    .build();

            this.tempCredentialService.createAndInvalidateOld(tempCredential);

            return securityKey;
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    public void requestMfaCode(UserDto user, AuthAdaptor authAdaptor, AuthResponseDto authResponseDto) throws ApplicationException {
        try{
            final String idpCode = UniqueCodeGenerator.generateCodeWithChar(4);
            final String securityKey = UniqueCodeGenerator.generateSecureKey();
            TempCredential tempCredential = TempCredential.builder()
                    .phoneNumber(user.getPhoneNumber()).credentialType(CredentialType.MFA_CODE).retryCount(0).securityKey(securityKey)
                    .id(idpCode).userId(user.getUserId()).authAdaptor(authAdaptor).email(user.getPrimaryEmailAddress())
                    .phoneNumber(user.getPrimaryPhoneNumber()).userName(user.getUserName()).build();

            authResponseDto.setIdpCode(idpCode);
            authResponseDto.setSecurityKey(securityKey);

            this.tempCredentialRepository.save(tempCredential);
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
}