package com.photon.identity.commons.properties;

import com.photon.identity.commons.properties.dto.OauthProviderConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "photon.oauth2")
public class OauthConfigProperties {
    private List<OauthProviderConfig> config;
}