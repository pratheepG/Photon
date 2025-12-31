package com.photon.identity.authentication.dto;

import com.photon.identity.commons.dto.request.CdnAssetInfoDto;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.ElectronicAddressType;
import com.photon.identity.commons.enums.Gender;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
public class UserDto {
	private String userId;
	private Set<UUID> tenantIds;
	private String subscriberId;
	private String firstName;
	private String lastName;
	private String userName;
	private String phoneNumber;
	private String countryCode;
	private String email;
	private Gender sex;
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
	private Date lastPasswordUpdatedOn;
	private Boolean isServerConsoleUser;
	private Boolean isMfaEnabled;

	@Builder.Default
	private Set<RoleDto> roles = new LinkedHashSet<>();

	@Builder.Default
	private Set<AddressDto> address = new LinkedHashSet<>();

	@Builder.Default
	private Set<ElectronicAddressDto> electronicAddress = new LinkedHashSet<>();

	@Builder.Default
	private Set<AuthAdaptor> activeAuthAdapters = new LinkedHashSet<>();

	private CdnAssetInfoDto profilePic;

	@Builder.Default
	private Map<String, Object> additionalAttributes = new LinkedHashMap<>();

	public UserDto(String userId, Set<UUID> tenantIds, String subscriberId, String firstName, String lastName, String userName, String countryCode, String phoneNumber, String email, Gender sex, Boolean isEnabled, Boolean isAccountNonExpired, Boolean isAccountNonLocked, Boolean isCredentialsNonExpired, int inValidLoginAttempts, String inValidLoginAttemptAuthType, Date dob, Date createdOn, Date updatedOn, Date lastMfaVerifiedOn, Date lastPasswordUpdatedOn, Boolean isServerConsoleUser, Boolean isMfaEnabled, Set<RoleDto> roles, Set<AddressDto> address, Set<ElectronicAddressDto> electronicAddress, Set<AuthAdaptor> activeAuthAdapters, CdnAssetInfoDto profilePic, Map<String, Object> additionalAttributes) {
		this.userId = userId;
		this.tenantIds = tenantIds;
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
		this.lastPasswordUpdatedOn = lastPasswordUpdatedOn;
		this.isServerConsoleUser = isServerConsoleUser;
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