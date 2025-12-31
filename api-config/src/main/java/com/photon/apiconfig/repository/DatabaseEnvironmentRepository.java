package com.photon.apiconfig.repository;

import com.photon.apiconfig.entity.ConfigProperties;
import com.photon.apiconfig.entity.mapper.ConfigPropertiesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DatabaseEnvironmentRepository implements EnvironmentRepository {

    private static final String LOGGING_POSTFIX = "-LOGGING";
    private static final String SETTINGS_POSTFIX = "-SETTINGS";
    private static final String API_CONFIG_POSTFIX = "-API-CONFIG";
    private static final String SERVER_PROPERTIES = "SERVER-PROPERTIES";

    private final ConfigPropertiesRepository configPropertiesRepository;
    private final ConfigPropertiesMapper configPropertiesMapper;

    @Autowired
    public DatabaseEnvironmentRepository(ConfigPropertiesRepository configPropertiesRepository,
                                         ConfigPropertiesMapper configPropertiesMapper) {
        this.configPropertiesRepository = configPropertiesRepository;
        this.configPropertiesMapper = configPropertiesMapper;
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        log.info("Fetching configuration for application={}, profile={}, label={}", application, profile, label);

        try {

            Optional<ConfigProperties> optionalAppConfigProperties = configPropertiesRepository.findByIdAndProfile(application, profile);
            Optional<ConfigProperties> optionalAppLoggingConfigProperties = configPropertiesRepository.findByIdAndProfile(application.concat(LOGGING_POSTFIX), profile);
            Optional<ConfigProperties> optionalAppSettingsConfigProperties = configPropertiesRepository.findByIdAndProfile(application.concat(SETTINGS_POSTFIX), profile);
            Optional<ConfigProperties> optionalApiConfigProperties = configPropertiesRepository.findByIdAndProfile(application.concat(API_CONFIG_POSTFIX), profile);
            Optional<ConfigProperties> optionalCommonConfigProperties = configPropertiesRepository.findByIdAndProfile(SERVER_PROPERTIES, profile);

            Map<String, String> properties = new HashMap<>();

            // If a default config is present, convert its JsonNode to Map and merge into properties.
            // Note: This will override any existing keys from the application-specific config if they conflict.
            optionalAppConfigProperties.ifPresent(appConfigProperties ->
                    properties.putAll(configPropertiesMapper.toConfigMap(appConfigProperties.getConfig()))
            );

            // If a logging config is present, convert its JsonNode to Map and merge into properties.
            // Note: This will override any existing keys from the application-specific config if they conflict.
            optionalAppLoggingConfigProperties.ifPresent(loggingConfigProperties ->
                    properties.putAll(configPropertiesMapper.toConfigMap(loggingConfigProperties.getConfig()))
            );

            // If a settings config is present, convert its JsonNode to Map and merge into properties.
            // Note: This will override any existing keys from the application-specific config if they conflict.
            optionalAppSettingsConfigProperties.ifPresent(settingsConfigProperties ->
                    properties.putAll(configPropertiesMapper.toConfigMap(settingsConfigProperties.getConfig()))
            );

            // If a api config is present, convert its JsonNode to Map and merge into properties.
            // Note: This will override any existing keys from the application-specific config if they conflict.
            optionalApiConfigProperties.ifPresent(apiConfigProperties ->
                    properties.putAll(configPropertiesMapper.toConfigMap(apiConfigProperties.getConfig()))
            );

            // If a common config is present, convert its JsonNode to Map and merge into properties.
            // Note: This will override any existing keys from the application-specific config if they conflict.
            optionalCommonConfigProperties.ifPresent(commonConfigProperties ->
                    properties.putAll(configPropertiesMapper.toConfigMap(commonConfigProperties.getConfig()))
            );

            // If no configuration was found at all, return an empty environment
            if (properties.isEmpty() && optionalAppConfigProperties.isEmpty() && optionalCommonConfigProperties.isEmpty()) {
                log.warn("No configuration found for application={}, profile={}", application, profile);
                return new Environment(application, profile);
            }

            PropertySource propertySource = new PropertySource(application + "-" + profile, properties);
            Environment environment = new Environment(application, profile);
            environment.add(propertySource);

            log.info("Successfully fetched configuration for application={}, profile={}", application, profile);
            return environment;

        } catch (Exception ex) {
            log.error("Error fetching configuration for application={}, profile={}", application, profile, ex);
            return new Environment(application, profile);
        }
    }
}