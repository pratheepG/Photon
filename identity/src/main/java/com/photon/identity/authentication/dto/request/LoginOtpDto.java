package com.photon.identity.authentication.dto.request;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = JsonDeserializer.None.class)
public class LoginOtpDto implements AbstractAuthRequest {
    @NotNull(message = "Principle can't be Null")
    @NotEmpty(message = "Principle can't be Empty")
    @NotBlank(message = "Principle can't be Blank")
    private String principle;
}