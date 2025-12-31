package com.photon.identity.commons.properties.dto;

import lombok.Data;

@Data
public class OauthProviderConfig {
    private String id;
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
    private String redirectUri;
    private String userNameAttributeName;
}