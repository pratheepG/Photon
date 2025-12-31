package com.photon.identity.commons.utils;

import com.photon.alerts.AlertProducer;
import com.photon.alerts.dto.AlertEventDto;
import com.photon.alerts.enums.AlertAudience;
import com.photon.alerts.enums.Channel;
import com.photon.alerts.enums.Priority;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.commons.enums.AuthAdaptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class IdentityAlertHandler {

    private final AlertProducer alerts;

    public IdentityAlertHandler(AlertProducer alerts) {
        this.alerts = alerts;
    }

    @Async
    public void dispatchUserEnabledSuccessAck(String userId, boolean isEnabled) throws ApplicationException {
        try {

            String statusMsg = isEnabled? "Enabled": "Disabled";
            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("statusMsg", statusMsg);

            String alertSubType = "USER_ENABLED";
            AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                    .alertType("USER_EVENT").alertSubType(alertSubType)
                    .placeholders(placeHolders).isPassword(false)
                    .alertAudience(AlertAudience.INDIVIDUAL)
                    .status("SUCCESS").userId(userId).build();
            this.alerts.sendAlert(Priority.LOW, alertEventDtoDto);
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Async
    public void dispatchUserChangePwdSuccessAck(UserDto user) throws ApplicationException {
        try {

            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("fullName", user.getFirstName().concat(" ").concat(user.getLastName()));

            String alertSubType = "USER_PASSWORD_CHANGE";
            AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                    .alertType("AUTHENTICATION").alertSubType(alertSubType)
                    .placeholders(placeHolders).isPassword(false)
                    .alertAudience(AlertAudience.INDIVIDUAL)
                    .status("SUCCESS").userId(user.getUserId()).build();
            this.alerts.sendAlert(Priority.LOW, alertEventDtoDto);
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Async
    public void dispatchUserRegistrationAck(Map<String, String> placeHolders, User user, Set<AuthAdaptor> adaptors) throws ApplicationException {
        try {
            String alertSubType = "BACK_OFFICE_ON_BOARDING";
            adaptors.forEach(adaptor -> {

                AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                        .alertType("CREATE_USER").alertSubType(alertSubType)
                        .placeholders(placeHolders).isPassword(false)
                        .alertAudience(AlertAudience.INDIVIDUAL)
                        .status("SUCCESS")
                        .build();

                if(adaptor.equals(AuthAdaptor.EML_OTP)) {
                    alertEventDtoDto.setEmail(user.getEmail());
                    alertEventDtoDto.setChannel(Channel.E_MAIL);
                } else if(adaptor.equals(AuthAdaptor.SMS_OTP)) {
                    alertEventDtoDto.setPhoneNumber(user.getPhoneNumber());
                    alertEventDtoDto.setCountryCode(user.getCountryCode());
                    alertEventDtoDto.setChannel(Channel.SMS);
                }
                this.alerts.sendAlert(Priority.LOW, alertEventDtoDto);
            });
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Async
    public void dispatchUserTempPwdForRegister(Map<String, String> placeHolders, String userId) throws ApplicationException {
        try {
            String alertSubType = "LOGIN_TMP_PWD";
            AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                    .alertType("AUTHENTICATION").alertSubType(alertSubType)
                    .placeholders(placeHolders).isPassword(true)
                    .alertAudience(AlertAudience.INDIVIDUAL)
                    .status("SUCCESS")
                    .userId(userId)
                    .build();
            this.alerts.sendAlert(Priority.HIGH, alertEventDtoDto);
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @Async
    public void dispatchUserTempPwdForResetPwd(Map<String, String> placeHolders, String userId) throws ApplicationException {
        try {
            String alertSubType = "LOGIN_RESET_PWD";
            AlertEventDto alertEventDtoDto = AlertEventDto.builder()
                    .alertType("AUTHENTICATION").alertSubType(alertSubType)
                    .placeholders(placeHolders).isPassword(true)
                    .alertAudience(AlertAudience.INDIVIDUAL)
                    .status("SUCCESS")
                    .userId(userId)
                    .build();
            this.alerts.sendAlert(Priority.HIGH, alertEventDtoDto);
        } catch (ApplicationException ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

}