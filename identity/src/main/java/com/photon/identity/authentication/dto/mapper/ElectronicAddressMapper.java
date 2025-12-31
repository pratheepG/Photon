package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.ElectronicAddressDto;
import com.photon.identity.authentication.entity.ElectronicAddress;

public class ElectronicAddressMapper {

    public static ElectronicAddressDto toDto(ElectronicAddress electronicAddress) {
        if (electronicAddress == null) {
            return null;
        }

        return new ElectronicAddressDto(
                electronicAddress.getId(),
                electronicAddress.getType(),
                electronicAddress.getIsPrimary(),
                electronicAddress.getValue(),
                electronicAddress.getCountryCode()
        );
    }

    public static ElectronicAddress toEntity(ElectronicAddressDto electronicAddressDto) {
        if (electronicAddressDto == null) {
            return null;
        }

        ElectronicAddress electronicAddress = new ElectronicAddress();
        electronicAddress.setId(electronicAddressDto.getId());
        electronicAddress.setType(electronicAddressDto.getType());
        electronicAddress.setIsPrimary(electronicAddressDto.getIsPrimary());
        electronicAddress.setValue(electronicAddressDto.getValue());
        electronicAddress.setCountryCode(electronicAddressDto.getCountryCode());

        return electronicAddress;
    }

    public static ElectronicAddress partialUpdate(ElectronicAddressDto electronicAddressDto, ElectronicAddress electronicAddress) {
        if (electronicAddressDto == null || electronicAddress == null) {
            return electronicAddress;
        }

        if (electronicAddressDto.getId() != null) {
            electronicAddress.setId(electronicAddressDto.getId());
        }
        if (electronicAddressDto.getType() != null) {
            electronicAddress.setType(electronicAddressDto.getType());
        }
        if (electronicAddressDto.getIsPrimary() != null) {
            electronicAddress.setIsPrimary(electronicAddressDto.getIsPrimary());
        }
        if (electronicAddressDto.getValue() != null) {
            electronicAddress.setValue(electronicAddressDto.getValue());
        }
        if (electronicAddressDto.getCountryCode() != null) {
            electronicAddress.setCountryCode(electronicAddressDto.getCountryCode());
        }

        return electronicAddress;
    }
}