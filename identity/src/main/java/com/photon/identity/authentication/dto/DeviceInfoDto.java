package com.photon.identity.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfoDto {

    @NotBlank(message = "Device id cannot be blank")
    private String deviceId;

    @NotNull(message = "Device type cannot be null")
    private String deviceType;

    @Override
	public String toString() {
		return "DeviceInfo [deviceId=" + deviceId + ", deviceType=" + deviceType + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(deviceId, deviceType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceInfoDto other = (DeviceInfoDto) obj;
		return Objects.equals(deviceId, other.deviceId) && Objects.equals(deviceType, other.deviceType);
	}
    
}
