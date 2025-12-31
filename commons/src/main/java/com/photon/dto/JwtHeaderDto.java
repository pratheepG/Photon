package com.photon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtHeaderDto {
    private String idp;
    @JsonProperty("auth-type")
    private String authType;
    @JsonProperty("auth-adaptor")
    private String authAdaptor;
    @JsonProperty("alg")
    private String alg;
    @JsonProperty("typ")
    private String typ;
    @JsonProperty("cty")
    private String cty;
    @JsonProperty("kid")
    private String kid;
    @JsonProperty("jku")
    private String jku;
}