package com.photon.identity.authentication.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.photon.identity.authentication.utils.AuthRequestDeserializer;

@JsonDeserialize(using = AuthRequestDeserializer.class)
public interface AbstractAuthRequest {
}