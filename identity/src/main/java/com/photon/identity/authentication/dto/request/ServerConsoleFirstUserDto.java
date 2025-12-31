package com.photon.identity.authentication.dto.request;

import com.photon.identity.authentication.entity.Address;
import com.photon.identity.commons.entity.CdnAssetInfo;
import com.photon.identity.commons.enums.Gender;
import com.photon.utils.PhotonUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerConsoleFirstUserDto {
	private String firstName;
	private String lastName;
	private String userName;
	private String password;
	private Gender sex;
	private String dob;
	private Set<Address> address;
	private CdnAssetInfo profilePic;
	private Map<String, Object> additionalAttributes;
	@NotNull(message = "tenant can't be null")
	@NotEmpty(message = "tenant can't be empty")
	private TenantRequestDto tenant;

	public Date getDob() {
		return PhotonUtils.parseDate(this.dob);
	}
}