/**
 * 
 */
package com.photon.identity.authentication.dto;

/**
 * @author pratheepg
 *
 */
public class RefreshClaimRequestDto {

	private String deviceId;
	private String refreshToken;
	
	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}
	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	/**
	 * 
	 */
	public RefreshClaimRequestDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param deviceId
	 * @param refreshToken
	 */
	public RefreshClaimRequestDto(String deviceId, String refreshToken) {
		super();
		this.deviceId = deviceId;
		this.refreshToken = refreshToken;
	}
	@Override
	public String toString() {
		return "RefreshClaimRequestDto [deviceId=" + deviceId + ", refreshToken=" + refreshToken + "]";
	}
	
}
