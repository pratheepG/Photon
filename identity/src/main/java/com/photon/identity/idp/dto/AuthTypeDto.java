package com.photon.identity.idp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.idp.entity.AuthType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link AuthType}
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthTypeDto implements Serializable {
    @NotNull(message = "AuthType id can't be Null")
    @NotEmpty(message = "AuthType id can't be Empty")
    @NotBlank(message = "AuthType id can't be Blank")
    String id;
    String name;
    String description;
    Boolean isActive;
    AuthAdaptor authAdapter;
    Object config;
}