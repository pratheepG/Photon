package com.photon.apiconfig.service;

import com.photon.apiconfig.dto.ConfigPropertiesDto;
import com.photon.apiconfig.entity.ConfigProperties;
import com.photon.apiconfig.entity.mapper.ConfigPropertiesMapper;
import com.photon.apiconfig.repository.ConfigPropertiesRepository;
import com.photon.apiconfig.utils.ConfigPropertiesActuatorUtils;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ConfigPropertiesService {

    private final ConfigPropertiesRepository configRepository;
    private final ConfigPropertiesActuatorUtils configPropertiesActuatorUtils;
    private final ConfigPropertiesMapper configPropertiesMapper;

    @Value("${photon.application.profile}")
    private String profile;

    @Autowired
    public ConfigPropertiesService(ConfigPropertiesRepository configRepository,
                                   ConfigPropertiesActuatorUtils configPropertiesActuatorUtils,
                                   ConfigPropertiesMapper configPropertiesMapper) {
        this.configRepository = configRepository;
        this.configPropertiesActuatorUtils = configPropertiesActuatorUtils;
        this.configPropertiesMapper = configPropertiesMapper;
    }

    /**
     * Retrieves configuration properties for a given application ID.
     * @param applicationId The ID of the application.
     * @return An ApiResponseDto containing the ConfigPropertiesDto.
     * @throws ApplicationException if configuration is not found or an internal error occurs.
     */
    public ApiResponseDto<ConfigPropertiesDto> getConfigForApplication(String applicationId) throws ApplicationException {
        try {
            log.info("getConfigForApplication input param applicationId: {}", applicationId);
            ConfigPropertiesDto config = configRepository.findById(applicationId)
                    .map(configPropertiesMapper::toDto)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Configuration not found for applicationId: " + applicationId),
                            HttpStatus.NOT_FOUND));

            return SuccessEnum.SUCCESS.getSuccessResponseBody(config);

        } catch (ApplicationException ae) {
            log.error("Application-specific exception in getConfigForApplication for applicationId {}: {}", applicationId, ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getConfigForApplication for applicationId {}: {}", applicationId, e.getMessage(), e); // Log full exception
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Partially updates the configuration properties for a given ID.
     * This method merges the incoming config data with the existing config data.
     * @param configDto The DTO containing the partial configuration updates.
     * @param id The ID of the configuration to patch.
     * @return An ApiResponseDto indicating success or failure.
     * @throws ApplicationException if configuration is not found or an internal error occurs.
     */
    @Transactional
    public ApiResponseDto<?> patchConfig(ConfigPropertiesDto configDto, String id) throws ApplicationException {
        try {
            log.debug("patchConfig input param applicationId: {}", id);
            Optional<ConfigProperties> configOptional = this.configRepository.findById(id);

            if (configOptional.isPresent()) {
                ConfigProperties config = configOptional.get();

                Map<String, String> incomingConfigMap = configDto.getConfig();

                if (incomingConfigMap != null) {
                    Map<String, String> currentConfigMap = configPropertiesMapper.toConfigMap(config.getConfig());

                    incomingConfigMap.forEach((key, val) -> {
                        if(StringUtils.isEmpty(val))
                            currentConfigMap.remove(key);
                        else
                            currentConfigMap.put(key, val);
                    });
                    config.setConfig(configPropertiesMapper.toConfigJsonNode(currentConfigMap));
                }
                // If incomingConfigMap is null, no patch data was provided, so no change to config.
                // If incomingConfigMap is empty, it will result in an empty config if currentConfigMap was empty.
                // If you want to explicitly clear config when an empty map is sent, you'd add:
                // else if (incomingConfigMap != null && incomingConfigMap.isEmpty()) {
                //    config.setConfig(configPropertiesMapper.toConfigJsonNode(new HashMap<>()));
                // }


                this.configRepository.save(config);
                if (!id.equalsIgnoreCase("SERVER-PROPERTIES") && !id.equalsIgnoreCase("CLIENT-PROPERTIES")) {
                    this.configPropertiesActuatorUtils.refreshConfig(id);
                }
                return SuccessEnum.UPDATED.getSuccessResponseBody();
            } else {
                configDto.setId(id);
                return this.createConfig(configDto);
            }

        } catch (ApplicationException ae) {
            log.error("Application-specific exception in patchConfig for applicationId {}: {}", id, ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in patchConfig for applicationId {}: {}", id, e.getMessage(), e); // Log full exception
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fully updates the configuration properties for a given ID.
     * This method replaces the entire config data with the new data from the DTO.
     * @param configDto The DTO containing the complete new configuration.
     * @param id The ID of the configuration to update.
     * @return An ApiResponseDto indicating success or failure.
     * @throws ApplicationException if configuration is not found or an internal error occurs.
     */
    @Transactional
    public ApiResponseDto<?> updateConfig(ConfigPropertiesDto configDto, String id) throws ApplicationException {
        try {
            log.debug("updateConfig input param applicationId: {}", id);
            Optional<ConfigProperties> configOptional = this.configRepository.findById(id);
            if (configOptional.isPresent()) {
                ConfigProperties config = configOptional.get();
                config.setConfig(configPropertiesMapper.toConfigJsonNode(configDto.getConfig()));

                this.configRepository.save(config);
                if (!id.equalsIgnoreCase("SERVER-PROPERTIES") && !id.equalsIgnoreCase("CLIENT-PROPERTIES")) {
                    this.configPropertiesActuatorUtils.refreshConfig(id);
                }
                return SuccessEnum.UPDATED.getSuccessResponseBody();
            } else {
                throw new ApplicationException(ExceptionEnum.ERR_1006.
                        getErrorResponseBody("Configuration not found for applicationId: " + id), HttpStatus.NOT_FOUND);
            }

        } catch (ApplicationException ae) {
            log.error("Application-specific exception in updateConfig for applicationId {}: {}", id, ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in updateConfig for applicationId {}: {}", id, e.getMessage(), e); // Log full exception
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates new configuration properties.
     * @param configDto The DTO containing the new configuration data.
     * @return An ApiResponseDto indicating success or failure.
     * @throws ApplicationException if an internal error occurs.
     */
    @Transactional
    public ApiResponseDto<?> createConfig(ConfigPropertiesDto configDto) throws ApplicationException {
        try {
            log.debug("createConfig input param applicationId: {}", configDto.getId());
            configDto.setProfile(this.profile);
            this.configRepository.save(configPropertiesMapper.toEntity(configDto));
            this.configPropertiesActuatorUtils.refreshConfig(configDto.getId());
            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("Application-specific exception in createConfig for applicationId {}: {}", configDto.getId(), ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in createConfig for applicationId {}: {}", configDto.getId(), e.getMessage(), e); // Log full exception
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes configuration properties for a given application ID.
     * @param applicationId The ID of the configuration to delete.
     * @return An ApiResponseDto indicating success or failure.
     * @throws ApplicationException if deletion fails or an internal error occurs.
     */
    @Transactional
    public ApiResponseDto<?> deleteConfig(String applicationId) throws ApplicationException {
        try {
            log.debug("deleteConfig input param applicationId: {}", applicationId);
            this.configRepository.deleteById(applicationId);

            if (this.configRepository.findById(applicationId).isEmpty()) {
                return SuccessEnum.DELETED.getSuccessResponseBody();
            } else {
                throw new ApplicationException(ExceptionEnum.ERR_1011.
                        getErrorResponseBody("Failed to delete config for applicationId: " + applicationId), HttpStatus.BAD_REQUEST);
            }
        } catch (ApplicationException ae) {
            log.error("Application-specific exception in deleteConfig for applicationId {}: {}", applicationId, ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in deleteConfig for applicationId {}: {}", applicationId, e.getMessage(), e); // Log full exception
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}