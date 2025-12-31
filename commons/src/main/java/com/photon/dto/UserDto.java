package com.photon.dto;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

	private String userId;
	private String firstName;
	private String lastName;
	private String userName;
	private String phoneNumber;
	private String password;
	private String sex;
	private String profilePicUrl;
	private Boolean isEnabled;
	private Date dob;
    private Date createdOn;
	private Date updatedOn;
	private List<AddressDto> address;
	private Set<String> roles;

}