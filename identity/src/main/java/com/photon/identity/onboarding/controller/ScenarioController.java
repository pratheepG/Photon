package com.photon.identity.onboarding.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.onboarding.dto.ScenarioRequestDto;
import com.photon.identity.onboarding.dto.ScenarioResponseDto;
import com.photon.identity.onboarding.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding/scenario")
public class ScenarioController {

    private final ScenarioService scenarioService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createScenario(@RequestBody ScenarioRequestDto dto) {
        return new ResponseEntity<>(scenarioService.create(dto), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponseDto<?>> createScenarios(@RequestBody List<ScenarioRequestDto> scenarios) {
        return ResponseEntity.ok(scenarioService.createBulk(scenarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ScenarioResponseDto>> getScenarioById(@PathVariable String id) {
        return ResponseEntity.ok(scenarioService.getById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ScenarioResponseDto>>> getAllScenarios(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scenarioService.getAll(page, size));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<?>> deleteScenarios(@RequestBody List<String> ids) {
        return ResponseEntity.ok(scenarioService.delete(ids));
    }
}