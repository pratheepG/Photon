package com.photon.identity.authentication.dto.request;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonDeserialize(using = JsonDeserializer.None.class)
public class VerifyOtpDto implements AbstractAuthRequest {
    @NotNull(message = "Password can't be Null")
    @NotEmpty(message = "Password can't be Empty")
    @NotBlank(message = "Password can't be Blank")
    private String password;
    @NotNull(message = "SecurityKey can't be Null")
    @NotEmpty(message = "SecurityKey can't be Empty")
    @NotBlank(message = "SecurityKey can't be Blank")
    private String securityKey;
}