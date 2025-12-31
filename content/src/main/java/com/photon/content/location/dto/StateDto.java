package com.photon.content.location.dto;

import lombok.*;
import java.util.List;

@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StateDto {
    private String name;
    private String code;
    private List<DistrictDto> districts;
}