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
public class LoginStaticPasswordDto implements AbstractAuthRequest  {
    @NotNull(message = "UserName can't be Null")
    @NotEmpty(message = "UserName can't be Empty")
    @NotBlank(message = "UserName can't be Blank")
    private String userName;
    @NotNull(message = "Password can't be Null")
    @NotEmpty(message = "Password can't be Empty")
    @NotBlank(message = "Password can't be Blank")
    private String password;
}