package com.photon.identity.commons.enums;

import com.photon.dto.ReportingParamsDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.authentication.dto.UserDeviceDto;
import com.photon.identity.authentication.dto.UserDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Getter
public enum MFAConditionType {

    NEW_DEVICE("New Device Detection") {
        @Override
        public boolean evaluate(UserDto userDto, UserDeviceDto userDeviceDto, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto) {
            return !userDeviceDto.getDeviceId().equals(reportingParamsDto.getDeviceId());
        }
    },
    EXPIRED_MFA("MFA Expiration Check") {
        @Override
        public boolean evaluate(UserDto userDto, UserDeviceDto userDeviceDto, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto) {
            Date lastValidated = userDto.getLastMfaVerifiedOn();
            if (Objects.isNull(lastValidated)) return true;
            LocalDateTime lastValidatedTime = lastValidated.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return lastValidatedTime.isBefore(LocalDateTime.now().minusMinutes(identityProviderDto.getLastValidateMfaExpiresInMinutes()));
        }
    },
    HIGH_RISK_IP("High-Risk IP Check") {
        @Override
        public boolean evaluate(UserDto userDto, UserDeviceDto userDeviceDto, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto) {
//            IpReputationService ipReputationService = SpringContextProvider.getBean(IpReputationService.class);
//            return ipReputationService.isHighRisk(loginRequestMFAInfoDto.getReportingParams().getIP());
            return false;
        }
    };

    private final String description;

    MFAConditionType(String description) {
        this.description = description;
    }

    public abstract boolean evaluate(UserDto userDto, UserDeviceDto userDeviceDto, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto);
}