package com.photon.console.controller;

import com.photon.console.dto.request.SnapshotUploadDto;
import com.photon.console.dto.response.DeploymentArtifactDetailsResponseDto;
import com.photon.console.dto.response.DeploymentArtifactResponseDto;
import com.photon.console.service.DeploymentService;
import com.photon.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deployment")
public class DeploymentController {

    private final DeploymentService deploymentService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<DeploymentArtifactResponseDto>>> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(deploymentService.getDeployments(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DeploymentArtifactDetailsResponseDto>> getById(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(deploymentService.getDeploymentDetailsById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteById(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(deploymentService.deleteArtifact(id));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponseDto<?>> upload(@ModelAttribute SnapshotUploadDto snapshotUploadDto) {
        return ResponseEntity.ok(deploymentService.uploadSnapshot(snapshotUploadDto));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponseDto<?>> deploy(@PathVariable String id) {
        return ResponseEntity.ok(deploymentService.deploySnapshot(id));
    }

    @PutMapping("/{id}/edit-dockerfile")
    public ResponseEntity<ApiResponseDto<?>> editDockerFile(@PathVariable String id, @ModelAttribute SnapshotUploadDto dto) {
        return ResponseEntity.ok(deploymentService.editDockerFile(id, dto));
    }

}