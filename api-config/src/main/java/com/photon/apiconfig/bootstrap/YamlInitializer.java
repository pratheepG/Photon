package com.photon.apiconfig.bootstrap;

import com.photon.apiconfig.event.ConfigReloadNotifier;
import com.photon.apiconfig.utils.YamlPlaceholderResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class YamlInitializer implements ApplicationRunner {

    @Value("${EXTERNAL_CONFIG_PATH_SOURCE:./config/application_original.yml}")
    private String sourcePath;

    @Value("${EXTERNAL_CONFIG_PATH_DEST:./config/application.yml}")
    private String destPath;


    @Override
    public void run(ApplicationArguments args) {
        try {
            YamlPlaceholderResolver.resolveAndWriteYaml(sourcePath, destPath);
            log.info("✅ Published ExternalYamlResolvedEvent for path: {} ", destPath);
        } catch (IOException ex) {
            log.error("Error found while resolving {application_original} placeholders : {}",ex.getMessage());
        }
    }
}