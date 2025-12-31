package com.photon.endpoint.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ModelDescriptionDto {
    private String id;
    private String name;
    private List<ModelFieldDto> fields = new ArrayList<>();
}