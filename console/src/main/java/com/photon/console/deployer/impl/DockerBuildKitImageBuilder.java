package com.photon.console.deployer.impl;

import com.photon.console.deployer.ImageBuilder;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class DockerBuildKitImageBuilder implements ImageBuilder {

    /**
     * Build and push an image using the build context at tmpDir.
     *
     * @param tmpDir   directory with Dockerfile and app.jar
     * @param baseName base image name (e.g. micro-service)
     * @param env      environment variables (used for metadata or optional templates)
     * @return fully qualified image tag (e.g. registry.example.com/micro-service:sha123)
     */
    @Override
    public String buildAndPushImage(Path tmpDir, String baseName, Map<String, String> env) throws Exception {
        return "";
    }

    @Override
    public String buildImage(Path contextDir, String imageName) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "docker", "build",
                "-t", imageName + ":latest",
                contextDir.toAbsolutePath().toString()
        );

        pb.environment().put("DOCKER_BUILDKIT", "1");
        pb.inheritIO();

        Process p = pb.start();
        int exit = p.waitFor();

        if (exit != 0) {
            throw new RuntimeException("Docker build failed with exit code " + exit);
        }

        return imageName + ":latest";
    }
}