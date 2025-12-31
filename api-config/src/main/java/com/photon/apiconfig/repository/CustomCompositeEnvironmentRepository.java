package com.photon.apiconfig.repository;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomCompositeEnvironmentRepository implements EnvironmentRepository {

    private final NativeEnvironmentRepository nativeEnvironmentRepository;
    private final DatabaseEnvironmentRepository databaseEnvironmentRepository;

    public CustomCompositeEnvironmentRepository(NativeEnvironmentRepository nativeEnvironmentRepository,
                                          DatabaseEnvironmentRepository databaseEnvironmentRepository) {
        this.nativeEnvironmentRepository = nativeEnvironmentRepository;
        this.databaseEnvironmentRepository = databaseEnvironmentRepository;
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        Environment dbEnvironment = databaseEnvironmentRepository.findOne(application, profile, label);
        Environment fileEnvironment = nativeEnvironmentRepository.findOne(application, profile, label);

        if (dbEnvironment.getPropertySources().isEmpty()) {
            return fileEnvironment;
        }

        for (PropertySource source : fileEnvironment.getPropertySources()) {
            dbEnvironment.add(source);
        }

        return dbEnvironment;
    }
}