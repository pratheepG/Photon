package com.photon.identity.authentication.dto;

import com.photon.identity.authentication.entity.UserLoginHistory;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.LoginStatus;
import com.photon.identity.commons.enums.LoginFactor;
import com.photon.identity.idp.dto.AuthTypeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * DTO for {@link UserLoginHistory}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginHistoryDto implements Serializable {
    private String sessionId;
    private String userId;
    private String loginPrinciple;
    private AuthTypeDto authType;
    private AuthAdaptor authAdaptor;
    private LoginFactor loginFactor;
    private Boolean isSuccess;
    private Boolean isRetry;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String deviceId;
    private String failureReason;
    private Date loginTimestamp;
    private LoginStatus loginStatus;
    private Date createdOn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLoginHistoryDto that = (UserLoginHistoryDto) o;
        return Objects.equals(authType, that.authType) && (Objects.equals(loginTimestamp, that.loginTimestamp) || loginStatus == that.loginStatus);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(authType);
        if (loginTimestamp != null) {
            result = 31 * result + loginTimestamp.hashCode();
        } else if (loginStatus != null) {
            result = 31 * result + loginStatus.hashCode();
        }
        return result;
    }
}