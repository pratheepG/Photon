package com.photon.identity.authentication.dto;

import lombok.*;

import java.util.Date;


/**
 * @author pratheepg
 *
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceDto {

    private Long id;
    private String userId;
    private String deviceType;
    private String deviceId;
    private Boolean isActive;
    private RefreshTokenDto refreshToken;
    private Boolean isRefreshActive;
    private Date createdOn;
    private Date modifiedOn;

	@Override
	public String toString() {
		return "UserDeviceDto [id=" + id + ", userId=" + userId + ", deviceType=" + deviceType + ", deviceId=" + deviceId
				+ ", isActive=" + isActive + ", refreshToken=" + refreshToken + ", isRefreshActive=" + isRefreshActive
				+ ", createdOn=" + createdOn + ", modifiedOn=" + modifiedOn + "]";
	}
	
}
