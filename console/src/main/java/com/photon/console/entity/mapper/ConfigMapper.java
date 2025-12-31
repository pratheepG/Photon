package com.photon.console.entity.mapper;

import com.photon.console.dto.ConfigDto;
import com.photon.console.entity.Config;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConfigMapper {
    Config toEntity(ConfigDto configDto);

    ConfigDto toDto(Config config);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Config partialUpdate(ConfigDto configDto, @MappingTarget Config config);
}