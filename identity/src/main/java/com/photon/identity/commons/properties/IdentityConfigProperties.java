package com.photon.identity.commons.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class IdentityConfigProperties {
    @Value("${photon.identity.temp.pwd.exp:36000}")
    private Long tmpPwdExpInMinutes;
}