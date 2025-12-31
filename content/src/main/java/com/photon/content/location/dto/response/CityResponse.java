package com.photon.content.location.dto.response;

import lombok.*;

@Data
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityResponse {
    private Long id;
    private String name;
    private String pinCode;
    private Long districtId;
}