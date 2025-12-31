package com.photon.identity.authentication.dto;

import com.photon.identity.authentication.entity.ElectronicAddress;
import com.photon.identity.commons.enums.ElectronicAddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link ElectronicAddress}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElectronicAddressDto implements Serializable {
    private Long id;
    private ElectronicAddressType type;
    private Boolean isPrimary = false;
    private String value;
    private String countryCode;
}