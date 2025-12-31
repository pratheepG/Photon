package com.photon.identity.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.onboarding.dto.*;
import com.photon.identity.onboarding.dto.request.FormRequestDto;
import com.photon.identity.onboarding.dto.response.FormListItemDto;
import com.photon.identity.onboarding.dto.response.FormPreviewDto;
import com.photon.identity.onboarding.entity.*;
import com.photon.identity.onboarding.enums.StructureItemType;
import com.photon.identity.onboarding.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final GroupRepository groupRepository;
    private final FormStructureMapRepository formStructureMapRepository;
    private final GroupFieldMapRepository groupFieldMapRepository;
    private final FieldRepository fieldRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiResponseDto<?> createForm(FormRequestDto dto) {
        try {
            Form form = Form.builder().name(dto.getName()).build();
            formRepository.save(form);

            for (FormStructureDto field : dto.getFields()) {
                handleStructure(form, field);
            }

            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (Exception e) {
            log.error("Error while creating form structure: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> patchForm(Long formId, FormRequestDto dto) {
        try {
            Form form = formRepository.findById(formId)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Form not found"), HttpStatus.NOT_FOUND));

            // Update form name if changed
            if (dto.getName() != null) {
                form.setName(dto.getName());
                formRepository.save(form);
            }

            for (FormStructureDto field : dto.getFields()) {

                if (field.getType() == StructureItemType.FIELD) {
                    boolean exists = formStructureMapRepository.existsByFormIdAndReferenceIdAndType(
                            formId, field.getReferenceId(), StructureItemType.FIELD);

                    if (!exists) {
                        // New field mapping
                        formStructureMapRepository.save(FormStructureMap.builder()
                                .form(form)
                                .referenceId(field.getReferenceId())
                                .type(StructureItemType.FIELD)
                                .required(field.isRequired())
                                .dtoFieldName(field.getDtoFieldName())
                                .build());
                    } else {
                        // Update existing field mapping
                        int modifiedRows = formStructureMapRepository.updateStructureDetails(
                                formId,
                                field.getReferenceId(),
                                field.isRequired(),
                                field.getDtoFieldName(),
                                StructureItemType.FIELD
                        );

                        log.info("Updated FormStructureMap rows: {}", modifiedRows);
                    }

                } else if (field.getType() == StructureItemType.GROUP) {
                    // Create or fetch group
                    Group group;
                    if (field.getReferenceId() == null) {
                        group = Group.builder()
                                .name(field.getDtoFieldName())
                                .isCollection(field.isCollection())
                                .build();
                        group = groupRepository.save(group);
                    } else {
                        group = groupRepository.findById(field.getReferenceId())
                                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Group not found"), HttpStatus.BAD_REQUEST));

                        group.setName(field.getDtoFieldName());
                        group.setCollection(field.isCollection());
                        groupRepository.save(group);
                    }

                    boolean exists = formStructureMapRepository.existsByFormIdAndReferenceIdAndType(
                            formId, group.getId(), StructureItemType.GROUP);

                    if (!exists) {
                        formStructureMapRepository.save(FormStructureMap.builder()
                                .form(form)
                                .referenceId(group.getId())
                                .type(StructureItemType.GROUP)
                                .required(field.isRequired())
                                .dtoFieldName(field.getDtoFieldName())
                                .build());
                    } else {
                        formStructureMapRepository.updateStructureDetails(
                                formId,
                                group.getId(),
                                field.isRequired(),
                                field.getDtoFieldName(),
                                StructureItemType.GROUP
                        );
                    }

                    // Update fields inside group
                    for (FormStructureDto inner : field.getData()) {
                        boolean mapped = groupFieldMapRepository.existsByGroupIdAndFieldId(
                                group.getId(), inner.getReferenceId());

                        if (!mapped) {
                            groupFieldMapRepository.save(GroupFieldMap.builder()
                                    .group(group)
                                    .field(fieldRepository.getReferenceById(inner.getReferenceId()))
                                    .isRequired(inner.isRequired())
                                    .dtoFieldName(inner.getDtoFieldName())
                                    .build());
                        } else {
                            groupFieldMapRepository.updateGroupFieldDetails(
                                    group.getId(),
                                    inner.getReferenceId(),
                                    inner.isRequired(),
                                    inner.getDtoFieldName()
                            );
                        }
                    }
                }
            }

            return SuccessEnum.UPDATED.getSuccessResponseBody();

        } catch (Exception e) {
            log.error("Error while patching form structure: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Unable to patch form"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<FormListItemDto>> getAllForms(int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<Form> formPage = formRepository.findAll(pageable);

            List<FormListItemDto> formList = formPage.getContent().stream()
                    .map(form -> FormListItemDto.builder().id(form.getId()).name(form.getName()).build())
                    .toList();

            return SuccessEnum.SUCCESS.getSuccessResponseBody(formList, null, pageable.getPageNumber(), formList.size(), formPage.getTotalPages(), (int) formPage.getTotalElements());

        } catch (Exception e) {
            log.error("Exception in getAllForms: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Unable to fetch form list"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<FormPreviewDto> getFormPreview(Long formId) {
        try {
            Form form = formRepository.findById(formId).orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Form not found"), HttpStatus.NOT_FOUND));

            List<FormStructureMap> mappings = formStructureMapRepository.findByFormIdOrderById(formId);

            List<FormElementDto> elements = new ArrayList<>();

            for (FormStructureMap item : mappings) {
                if (item.getType() == StructureItemType.FIELD) {
                    Field field = fieldRepository.findById(item.getReferenceId())
                            .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Field not found"), HttpStatus.BAD_REQUEST));
                    FieldDto fieldDto = FieldDto.builder()
                            .id(field.getId())
                            .name(field.getName())
                            .type(field.getType())
                            .isCollection(field.isCollection())
                            .config(objectMapper.readValue(field.getField(), Object.class)).build();

                    elements.add(FormElementDto.builder()
                            .type(StructureItemType.FIELD)
                            .isRequired(item.isRequired())
                            .dtoFieldName(item.getDtoFieldName())
                            .field(fieldDto).build());

                } else if (item.getType() == StructureItemType.GROUP) {
                    Group group = groupRepository.findById(item.getReferenceId())
                            .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Group not found"), HttpStatus.BAD_REQUEST));

                    List<GroupFieldMap> fieldMaps = groupFieldMapRepository.findByGroupId(group.getId());
                    List<GroupFieldDto> groupFields = new ArrayList<>();

                    for (GroupFieldMap map : fieldMaps) {
                        Field field = map.getField();
                        groupFields.add(GroupFieldDto.builder().required(map.isRequired())
                                .field(FieldDto.builder()
                                        .id(field.getId())
                                        .name(field.getName())
                                        .type(field.getType())
                                        .isCollection(field.isCollection()).config(objectMapper.readValue(field.getField(), Object.class))
                                        .build())
                                .build());
                    }

                    elements.add(FormElementDto.builder().type(StructureItemType.GROUP).isRequired(item.isRequired())
                            .group(GroupDto.builder()
                                    .id(group.getId())
                                    .name(group.getName())
                                    .isCollection(group.isCollection())
                                    .fields(groupFields).build())
                            .dtoFieldName(item.getDtoFieldName())
                            .build());
                }
            }

            return SuccessEnum.SUCCESS.getSuccessResponseBody(FormPreviewDto.builder().id(form.getId())
                    .name(form.getName()).fields(elements).build());
        } catch (JsonProcessingException jex) {
            log.error("Error while fetching form: {}", jex.getMessage(), jex);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Form preview failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleStructure(Form form, FormStructureDto dto) {
        if (dto.getType() == StructureItemType.FIELD) {
            formStructureMapRepository.save(FormStructureMap.builder()
                    .form(form).referenceId(dto.getReferenceId()).type(StructureItemType.FIELD)
                    .required(dto.isRequired()).build());

        } else if (dto.getType() == StructureItemType.GROUP) {
            Group group = Group.builder()
                    .name(dto.getDtoFieldName()).isCollection(dto.isCollection()).build();

            group = groupRepository.save(group);

            formStructureMapRepository.save(FormStructureMap.builder()
                    .dtoFieldName(dto.getDtoFieldName()).form(form).referenceId(group.getId()).type(StructureItemType.GROUP)
                    .required(dto.isRequired()).build());

            if (dto.getData() != null) {
                for (FormStructureDto inner : dto.getData()) {
                    if (inner.getType() == StructureItemType.FIELD) {
                        groupFieldMapRepository.save(GroupFieldMap.builder()
                                .group(group).field(fieldRepository.getReferenceById(inner.getReferenceId()))
                                .dtoFieldName(inner.getDtoFieldName()).isRequired(inner.isRequired()).build());
                    } else {
                        throw new UnsupportedOperationException("Nested groups not supported inside a group");
                    }
                }
            }
        }
    }

}