package com.photon.identity.authentication.dto.request;

import com.photon.identity.authentication.dto.AddressDto;
import com.photon.identity.authentication.dto.ElectronicAddressDto;
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
public class UserRequestDto {
    private String firstName;
    private String lastName;
    private String userName;
    private Gender sex;
    private String dob;

    @NotNull(message = "tenantId can't be null")
    @NotEmpty(message = "tenantId can't be empty")
    private String tenantId;
    private Set<AddressDto> address;

    @NotNull(message = "electronicAddress can't be null")
    @NotEmpty(message = "electronicAddress can't be empty")
    private Set<ElectronicAddressDto> electronicAddress;
    private Map<String, Object> additionalAttributes;

    public Date getDob() {
        return PhotonUtils.parseDate(this.dob);
    }
}