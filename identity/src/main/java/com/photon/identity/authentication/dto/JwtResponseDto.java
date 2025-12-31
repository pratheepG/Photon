package com.photon.identity.authentication.dto;

import java.util.Map;

public class JwtResponseDto {
	 
    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiryDuration;
    
    private Map<String, Object> userData;
    
    public JwtResponseDto(String accessToken, String refreshToken, Long expiryDuration, Map<String, Object> userData) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryDuration = expiryDuration;
        this.userData = userData;
        tokenType = "Bearer ";
    }

	/**
	 * @return the userData
	 */
	public Map<String, Object> getUserData() {
		return userData;
	}

	/**
	 * @param userData the userData to set
	 */
	public void setUserData(Map<String, Object> userData) {
		this.userData = userData;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public Long getExpiryDuration() {
		return expiryDuration;
	}

	public void setExpiryDuration(Long expiryDuration) {
		this.expiryDuration = expiryDuration;
	}

	public JwtResponseDto() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}
