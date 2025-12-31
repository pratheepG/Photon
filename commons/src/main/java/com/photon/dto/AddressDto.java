package com.photon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto implements Serializable {
    private Integer id;
    private String addressLabel;
    private String streetName;
    private String houseNumber;
    private String city;
    private String pin;
    private String district;
    private String state;
}