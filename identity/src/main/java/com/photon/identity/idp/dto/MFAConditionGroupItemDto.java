package com.photon.identity.idp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.photon.identity.commons.enums.MFAConditionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MFAConditionGroupItemDto {
    private Long id;
    private String operator;
    private MFAConditionType condition;
}
