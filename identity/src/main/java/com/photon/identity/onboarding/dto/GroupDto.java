package com.photon.identity.onboarding.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
public class GroupDto {
    private Long id;
    private String name;
    private boolean isCollection;
    private List<GroupFieldDto> fields;
}