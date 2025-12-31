package com.photon.console.service;

import com.photon.console.deployer.Deployer;
import com.photon.console.deployer.ImageBuilder;
import com.photon.console.dto.request.SnapshotUploadDto;
import com.photon.console.dto.response.DeploymentArtifactDetailsResponseDto;
import com.photon.console.dto.response.DeploymentArtifactResponseDto;
import com.photon.console.entity.DeploymentArtifact;
import com.photon.console.repository.DeploymentRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentService {

    private final DeploymentRepository artifactRepository;
    private final ImageBuilder imageBuilder;
    private final Deployer deployer;
    private final Semaphore buildSemaphore = new Semaphore(2);

    @Transactional
    public ApiResponseDto<DeploymentArtifactDetailsResponseDto> getDeploymentDetailsById(String id) {
        DeploymentArtifactDetailsResponseDto deploymentArtifact = this.artifactRepository
                .findDetailsById(UUID.fromString(id)).orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND));
        return SuccessEnum.SUCCESS.getSuccessResponseBody(deploymentArtifact);
    }

    public ApiResponseDto<List<DeploymentArtifactResponseDto>> getDeployments(int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return SuccessEnum.SUCCESS.getSuccessResponseBody(this.artifactRepository.findAllDtos(pageRequest));
    }

    public ApiResponseDto<?> deleteArtifact(String artifactId) {
        try {
            UUID id = UUID.fromString(artifactId);
            this.artifactRepository.deleteById(id);
            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (Exception e) {
            log.error("delete snapshot failed: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> uploadSnapshot(SnapshotUploadDto dto) {
        try {
            Map<String, String> envMap = dto.getEnv();
            String appId = UUID.randomUUID().toString();
            String appSecret = UUID.randomUUID().toString();
            envMap.put("APP_ID", appId);
            envMap.put("APP_SECRET", appSecret);

            DeploymentArtifact artifact = DeploymentArtifact.builder().serviceName(dto.getServiceName()).jarFileName(dto.getJar().getOriginalFilename()).jarContent(dto.getJar().getBytes()).dockerfileContent(dto.getDockerfile()).environment(envMap).appId(appId).appSecret(appSecret).deployed(false).build();

            this.artifactRepository.save(artifact);
            return SuccessEnum.SUCCESS.getSuccessResponseBody("Uploaded: ID = " + artifact.getId());
        } catch (Exception e) {
            log.error("Upload snapshot failed: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> editDockerFile(String artifactId, SnapshotUploadDto dto) {
        try {
            UUID id = UUID.fromString(artifactId);
            int rows = this.artifactRepository.updateDockerfileContentById(id, dto.getDockerfile());
            if(rows > 0)
                return SuccessEnum.SUCCESS.getSuccessResponseBody("Docker file modified for the artifact_id = " + artifactId);
            else
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody("Failed to update the docker file"), HttpStatus.NOT_MODIFIED);
        } catch (Exception e) {
            log.error("Upload snapshot failed: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Upload / get methods left unchanged (omitted here for brevity)...
     * (Use your earlier implementations; keep uploadSnapshot, getDeployments, getDeploymentDetailsById)
     */
    public ApiResponseDto<?> deploySnapshot(String artifactId) {
        UUID id = UUID.fromString(artifactId);
        DeploymentArtifact artifact = artifactRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody("Artifact not found"),HttpStatus.NOT_FOUND));

        if (!isDockerfileAllowed(artifact.getDockerfileContent())) {
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Dockerfile contains disallowed instructions"), HttpStatus.BAD_REQUEST);
        }

        boolean acquired = false;

        try {
            acquired = buildSemaphore.tryAcquire(10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Server busy"), HttpStatus.TOO_MANY_REQUESTS);
            }

            Path tmpDir = Files.createTempDirectory("photon-apis" + safeName(artifact.getServiceName()));

            try {
                Files.write(tmpDir.resolve("app.jar"), artifact.getJarContent(), CREATE_NEW);
                Files.writeString(tmpDir.resolve("Dockerfile"), artifact.getDockerfileContent(), CREATE_NEW);

                String imageName = "api-" + safeName(artifact.getServiceName()).toLowerCase();

                String builtImage = imageBuilder.buildImage(tmpDir, imageName);
                deployer.deploy(builtImage, imageName, artifact.getEnvironment());

                artifact.setDeployed(true);
                artifactRepository.save(artifact);

                return SuccessEnum.SUCCESS.getSuccessResponseBody("Deployed successfully: " + artifact.getServiceName());

            } finally {
                Files.walk(tmpDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

        } catch (Exception e) {
            log.error("Deployment failed", e);
            throw new ApplicationException(
                    ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } finally {
            if (acquired) buildSemaphore.release();
        }
    }

    private static String safeName(String raw) {
        if (raw == null) return "service";
        return raw.replaceAll("[^a-zA-Z0-9\\-]", "-");
    }

    private static boolean isDockerfileAllowed(String dockerfile) {
        String lower = dockerfile == null ? "" : dockerfile.toLowerCase(Locale.ROOT);
        if (lower.contains("/var/run/docker.sock") || lower.contains("docker.sock")) return false;
        if (lower.contains("mount=\"/") || lower.contains("mount /")) return false;
        if (lower.contains("privileged")) return false;
        if (lower.contains("from scratch")) return false;
        return true;
    }
}