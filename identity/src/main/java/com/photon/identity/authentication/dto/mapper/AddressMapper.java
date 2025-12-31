package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.AddressDto;
import com.photon.identity.authentication.entity.Address;

public class AddressMapper {

    /**
     * Converts an Address entity to an AddressDto.
     * The User relationship is ignored as it is typically handled separately or on a higher level.
     *
     * @param address The Address entity.
     * @return The corresponding AddressDto, or null if the entity is null.
     */
    public static AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }

        return AddressDto.builder()
                .id(address.getId())
                .type(address.getType())
                .streetName(address.getStreetName())
                .houseNumber(address.getHouseNumber())
                .city(address.getCity())
                .cityId(address.getCityId())
                .pin(address.getPin())
                .district(address.getDistrict())
                .districtId(address.getDistrictId())
                .state(address.getState())
                .stateCode(address.getStateCode())
                .stateId(address.getStateId())
                .isPrimary(address.isPrimary())
                .build();
    }

    /**
     * Converts an AddressDto to an Address entity.
     * Note: This method does NOT set the 'user' relationship, nor the audit fields (createdDate, lastModifiedDate).
     *
     * @param addressDto The AddressDto.
     * @return The corresponding Address entity, or null if the DTO is null.
     */
    public static Address toEntity(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        Address address = new Address();
        address.setId(addressDto.getId());
        address.setType(addressDto.getType());
        address.setStreetName(addressDto.getStreetName());
        address.setHouseNumber(addressDto.getHouseNumber());
        address.setCity(addressDto.getCity());
        address.setCityId(addressDto.getCityId());
        address.setPin(addressDto.getPin());
        address.setDistrict(addressDto.getDistrict());
        address.setDistrictId(address.getDistrictId());
        address.setState(addressDto.getState());
        address.setStateCode(addressDto.getStateCode());
        address.setStateId(address.getStateId());
        address.setPrimary(addressDto.isPrimary());

        return address;
    }

    /**
     * Performs a partial update on an existing Address entity using non-null fields from the DTO.
     * Note: Primitives (like 'isPrimary') are updated regardless of whether they were explicitly set in the DTO
     * since the DTO uses the primitive 'boolean' (defaulting to false).
     *
     * @param addressDto The DTO containing the update values.
     * @param address The existing Address entity to update.
     * @return The updated Address entity, or the original entity if either input is null.
     */
    public static Address partialUpdate(AddressDto addressDto, Address address) {
        if (addressDto == null || address == null) {
            return address;
        }

        if (addressDto.getId() != null) {
            address.setId(addressDto.getId());
        }
        if (addressDto.getType() != null) {
            address.setType(addressDto.getType());
        }
        if (addressDto.getStreetName() != null) {
            address.setStreetName(addressDto.getStreetName());
        }
        if (addressDto.getHouseNumber() != null) {
            address.setHouseNumber(addressDto.getHouseNumber());
        }
        if (addressDto.getCity() != null) {
            address.setCity(addressDto.getCity());
        }
        if (addressDto.getPin() != null) {
            address.setPin(addressDto.getPin());
        }
        if (addressDto.getDistrict() != null) {
            address.setDistrict(addressDto.getDistrict());
        }
        if (addressDto.getDistrictId() != null) {
            address.setDistrictId(addressDto.getDistrictId());
        }
        if (addressDto.getState() != null) {
            address.setState(addressDto.getState());
        }
        if (addressDto.getStateCode() != null) {
            address.setStateCode(addressDto.getStateCode());
        }
        if (addressDto.getStateId() != null) {
            address.setStateId(addressDto.getStateId());
        }
        if (addressDto.getCityId() != null) {
            address.setCityId(addressDto.getCityId());
        }

        address.setPrimary(addressDto.isPrimary());


        return address;
    }
}