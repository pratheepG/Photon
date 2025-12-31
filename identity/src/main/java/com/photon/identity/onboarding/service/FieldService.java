package com.photon.identity.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.onboarding.dto.*;
import com.photon.identity.onboarding.dto.mapper.FieldMapper;
import com.photon.identity.onboarding.dto.request.FieldRequestDto;
import com.photon.identity.onboarding.entity.Field;
import com.photon.identity.onboarding.enums.FieldType;
import com.photon.identity.onboarding.repository.FieldRepository;
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
public class FieldService {

    private final FieldRepository fieldRepository;
    private final ObjectMapper objectMapper;

    public ApiResponseDto<?> deleteById(Long id) {
        try {
            fieldRepository.deleteById(id);
            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (Exception e){
            e.printStackTrace();
            log.error("Exception in deleteById: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1011.getErrorResponseBody("Unable to delete the field"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<FieldDto>> getAllFields(int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<Field> fieldPage = fieldRepository.findAll(pageable);

            List<FieldDto> fieldList = fieldPage.getContent().stream().map(FieldMapper::toDto).toList();

            return SuccessEnum.SUCCESS.getSuccessResponseBody(fieldList).page(pageable.getPageNumber()).size(fieldList.size())
                    .totalPages(fieldPage.getTotalPages()).totalRecords((int) fieldPage.getTotalElements());
        } catch (Exception e) {
            log.error("Exception in getAllFields: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Unable to fetch field list"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<?> create(FieldRequestDto dto) {
        try {
            boolean isCollection = false;
            Object concreteConfig = convertFieldConfig(dto.getFieldConfig(), dto.getType(), isCollection);

            String json = objectMapper.writeValueAsString(concreteConfig);

            Field field = Field.builder().name(dto.getName()).isCollection(isCollection)
                    .type(dto.getType()).field(json).build();

            fieldRepository.save(field);
            return SuccessEnum.CREATED.getSuccessResponseBody();

        } catch (JsonProcessingException e) {
            log.error("JSON processing error: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("Invalid Field Configuration"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Exception while saving field: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<?> createBulk(List<FieldRequestDto> dtoList) {
        try {
            List<Field> fields = new ArrayList<>();

            for (FieldRequestDto dto : dtoList) {
                boolean isCollection = false;
                Object config = dto.getFieldConfig();
                Object concreteConfig = convertFieldConfig(config, dto.getType(), isCollection);

                String json = objectMapper.writeValueAsString(concreteConfig);

                Field field = Field.builder().name(dto.getName()).isCollection(isCollection)
                        .type(dto.getType()).field(json).build();

                fields.add(field);
            }

            fieldRepository.saveAll(fields);
            return SuccessEnum.CREATED.getSuccessResponseBody();

        } catch (JsonProcessingException e) {
            log.error("JSON error: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("Invalid Field Configuration"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Exception in bulk field save: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Object convertFieldConfig(Object rawFieldConfig, FieldType type, boolean isCollection) {
        Object field = null;
        switch (type) {
            case CHECK_BOX -> {
                CheckBoxField checkBoxField = objectMapper.convertValue(rawFieldConfig, CheckBoxField.class);
                isCollection = checkBoxField.getMaxSelection()>1;
                field = checkBoxField;
            }
            case DROP_DOWN -> {
                DropDownField dropDownField = objectMapper.convertValue(rawFieldConfig, DropDownField.class);
                isCollection = dropDownField.isCollection();
                field = dropDownField;
            }
            case TEXT_BOX -> {
                isCollection = false;
                field = objectMapper.convertValue(rawFieldConfig, TextField.class);
            }
            case RADIO_BUTTON -> {
                isCollection = false;
                field = objectMapper.convertValue(rawFieldConfig, RadioButtonField.class);
            }
            case FILE -> {
                isCollection = false;
                field = objectMapper.convertValue(rawFieldConfig, FileField.class);
            }
            case SWITCH -> {
                isCollection = false;
                field = objectMapper.convertValue(rawFieldConfig, SwitchField.class);
            }
            case DATE -> {
                isCollection = false;
                field = objectMapper.convertValue(rawFieldConfig, DateField.class);
            }
        };
        return field;
    }
}