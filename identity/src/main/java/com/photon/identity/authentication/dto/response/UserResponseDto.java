package com.photon.identity.authentication.dto.response;

import com.photon.identity.authentication.dto.ElectronicAddressDto;
import com.photon.identity.authentication.entity.Address;
import com.photon.identity.authentication.entity.Role;
import com.photon.identity.commons.entity.CdnAssetInfo;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.ElectronicAddressType;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
public class UserResponseDto {
	private String userId;
	private String subscriberId;
	private String firstName;
	private String lastName;
	private String userName;
	private String phoneNumber;
	private String countryCode;
	private String email;
	private String sex;
	private Boolean isEnabled;
	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private int inValidLoginAttempts;
	private String inValidLoginAttemptAuthType;
	private Date dob;
	private Date createdOn;
	private Date updatedOn;
	private Date lastMfaVerifiedOn;
	private Boolean isMfaEnabled;

	@Builder.Default
	private Set<Role> roles = new LinkedHashSet<>();

	@Builder.Default
	private Set<Address> address = new LinkedHashSet<>();

	@Builder.Default
	private Set<ElectronicAddressDto> electronicAddress = new LinkedHashSet<>();

	@Builder.Default
	private Set<AuthAdaptor> activeAuthAdapters = new LinkedHashSet<>();

	private CdnAssetInfo profilePic;

	@Builder.Default
	private Map<String, Object> additionalAttributes = new LinkedHashMap<>();

	public UserResponseDto(String userId, String subscriberId, String firstName, String lastName, String userName, String countryCode, String phoneNumber, String email, String sex, Boolean isEnabled, Boolean isAccountNonExpired, Boolean isAccountNonLocked, Boolean isCredentialsNonExpired, int inValidLoginAttempts, String inValidLoginAttemptAuthType, Date dob, Date createdOn, Date updatedOn, Date lastMfaVerifiedOn, Boolean isMfaEnabled, Set<Role> roles, Set<Address> address, Set<ElectronicAddressDto> electronicAddress, Set<AuthAdaptor> activeAuthAdapters, CdnAssetInfo profilePic, Map<String, Object> additionalAttributes) {
		this.userId = userId;
		this.subscriberId = subscriberId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.countryCode = countryCode;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.sex = sex;
		this.isEnabled = isEnabled;
		this.isAccountNonExpired = isAccountNonExpired;
		this.isAccountNonLocked = isAccountNonLocked;
		this.isCredentialsNonExpired = isCredentialsNonExpired;
		this.inValidLoginAttempts = inValidLoginAttempts;
		this.inValidLoginAttemptAuthType = inValidLoginAttemptAuthType;
		this.dob = dob;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
		this.lastMfaVerifiedOn = lastMfaVerifiedOn;
		this.isMfaEnabled = isMfaEnabled;
		this.roles = roles;
		this.address = address;
		this.electronicAddress = electronicAddress;
		this.activeAuthAdapters = activeAuthAdapters;
		this.profilePic = profilePic;
		this.additionalAttributes = additionalAttributes;
	}

	public String getPrimaryPhoneNumber() {
		return electronicAddress.stream()
				.filter(ElectronicAddressDto::getIsPrimary)
				.collect(Collectors.toSet()).stream()
				.filter(e->e.getType().equals(ElectronicAddressType.PHONE))
				.findFirst().map(ElectronicAddressDto::getValue).orElse("");
	}

	public String getPrimaryEmailAddress() {
		return electronicAddress.stream()
				.filter(ElectronicAddressDto::getIsPrimary)
				.collect(Collectors.toSet()).stream()
				.filter(e->e.getType().equals(ElectronicAddressType.E_MAIL))
				.findFirst().map(ElectronicAddressDto::getValue).orElse("");
	}
}