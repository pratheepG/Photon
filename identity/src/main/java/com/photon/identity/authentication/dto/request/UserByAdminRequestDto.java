package com.photon.identity.authentication.dto.request;

import com.photon.identity.authentication.dto.AddressDto;
import com.photon.identity.authentication.dto.ElectronicAddressDto;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.Gender;
import com.photon.utils.PhotonUtils;
import lombok.*;

import java.util.*;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserByAdminRequestDto {
    private String tenantId;
    private String firstName;
    private String lastName;
    private String userName;
    private Gender sex;
    private String dob;
    private Boolean isMfaEnabled;
    private Set<Long> roles;
    private Set<AddressDto> address;
    private Set<ElectronicAddressDto> electronicAddress;
    private Map<String, Object> additionalAttributes;

    public Date getDob() {
        if(Objects.isNull(this.dob))
            return null;
        return PhotonUtils.parseDate(this.dob);
    }
}