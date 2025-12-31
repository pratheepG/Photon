package com.photon.identity.onboarding.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.onboarding.dto.request.GroupFieldMapRequestDto;
import com.photon.identity.onboarding.dto.request.GroupRequestDto;
import com.photon.identity.onboarding.entity.Field;
import com.photon.identity.onboarding.entity.Group;
import com.photon.identity.onboarding.entity.GroupFieldMap;
import com.photon.identity.onboarding.repository.FieldRepository;
import com.photon.identity.onboarding.repository.GroupFieldMapRepository;
import com.photon.identity.onboarding.repository.GroupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final FieldRepository fieldRepository;
    private final GroupFieldMapRepository groupFieldMapRepository;

    @Transactional
    public ApiResponseDto<?> create(GroupRequestDto dto) {
        try {
            Group group = Group.builder().name(dto.getName())
                    .isCollection(dto.isCollection()).build();

            groupRepository.save(group);

            for (GroupFieldMapRequestDto mapDto : dto.getFields()) {
                Field field = fieldRepository.findById(mapDto.getFieldId())
                        .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Field not found: " + mapDto.getFieldId()), HttpStatus.BAD_REQUEST));

                GroupFieldMap map = GroupFieldMap.builder().group(group)
                        .field(field).isRequired(mapDto.isRequired()).build();

                groupFieldMapRepository.save(map);
            }

            return SuccessEnum.CREATED.getSuccessResponseBody();

        } catch (Exception e) {
            log.error("Error while creating group: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Group creation failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}