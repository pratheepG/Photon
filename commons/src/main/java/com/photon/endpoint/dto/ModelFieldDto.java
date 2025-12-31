package com.photon.endpoint.dto;

import com.photon.endpoint.enums.BaseType;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ModelFieldDto {
    private String name;
    private BaseType type;
    private String referenceType;
}