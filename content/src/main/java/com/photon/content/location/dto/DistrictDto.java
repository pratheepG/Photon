package com.photon.content.location.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDto {
    private String name;
    private List<CityDto> cities;
}