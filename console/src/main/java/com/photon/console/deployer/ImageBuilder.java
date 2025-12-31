package com.photon.console.deployer;

import java.nio.file.Path;
import java.util.Map;


public interface ImageBuilder {
    /**
     * Build and push an image using the build context at tmpDir.
     *
     * @param tmpDir    directory with Dockerfile and app.jar
     * @param baseName  base image name (e.g. micro-service)
     * @param env       environment variables (used for metadata or optional templates)
     * @return fully qualified image tag (e.g. registry.example.com/micro-service:sha123)
     */
    String buildAndPushImage(Path tmpDir, String baseName, Map<String,String> env) throws Exception;

    String buildImage(Path contextDir, String imageName) throws Exception;
}