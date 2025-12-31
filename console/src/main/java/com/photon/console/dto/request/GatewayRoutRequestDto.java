package com.photon.console.dto.request;

import com.photon.auth.enums.AuthFilter;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRoutRequestDto {

    private String routeId;
    private String path;
    private String method;
    private String applicationId;
    private Set<String> roles;
    private Set<String> idps;
    private AuthFilter authFilter;

}