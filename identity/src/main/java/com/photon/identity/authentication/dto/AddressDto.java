package com.photon.identity.authentication.dto;

import com.photon.identity.authentication.entity.Address;
import com.photon.identity.commons.enums.AddressType;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link Address}
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto implements Serializable {
    Integer id;
    AddressType type;
    String streetName;
    String houseNumber;
    String pin;
    String district;
    String districtId;
    String state;
    String stateId;
    String stateCode;
    String city;
    String cityId;
    boolean isPrimary;
}