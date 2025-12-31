package com.photon.identity.onboarding.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GroupRequestDto {
    private String id;
    private String name;
    private boolean isCollection;
    private List<GroupFieldMapRequestDto> fields;
}