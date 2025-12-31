package com.photon.identity.authentication.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogOutRequestDto {

    @Valid
    @NotNull(message = "Device info cannot be null")
    private DeviceInfoDto deviceInfo;

    @Valid
    @NotNull(message = "Existing Token needs to be passed")
    private String token;

	public LogOutRequestDto(@Valid @NotNull(message = "Device info cannot be null") DeviceInfoDto deviceInfo,
			@Valid @NotNull(message = "Existing Token needs to be passed") String token) {
		super();
		this.deviceInfo = deviceInfo;
		this.token = token;
	}

	public LogOutRequestDto() {
		super();
	}
    
}

