package com.photon.content.location.dto.response;

import lombok.*;

@Data
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DistrictResponse {
    private Long id;
    private String name;
    private Long stateId;
    private String stateCode;
}