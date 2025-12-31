package com.photon.auth.enums;

import lombok.Getter;

@Getter
public enum AuthFilter {
    PUBLIC("-"),
    ANONYMOUS("BasicAuthGatewayFilter"),
    AUTHENTICATED("JWTAuthGatewayFilter"),
    PROSPECT("JWTProspectAuthGatewayFilter");

    final String bean;

    AuthFilter(String bean) {
        this.bean = bean;
    }
}