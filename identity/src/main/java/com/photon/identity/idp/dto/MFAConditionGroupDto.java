package com.photon.identity.idp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MFAConditionGroupDto {
    private Long id;
    private String operator;
    private Set<MFAConditionGroupItemDto> items;
}
