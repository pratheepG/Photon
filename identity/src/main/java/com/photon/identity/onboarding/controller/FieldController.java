package com.photon.identity.onboarding.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.onboarding.dto.FieldDto;
import com.photon.identity.onboarding.dto.request.FieldRequestDto;
import com.photon.identity.onboarding.service.FieldService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding/field")
public class FieldController {

    private final FieldService fieldService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createField(@RequestBody FieldRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fieldService.create(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponseDto<?>> createFields(@RequestBody List<FieldRequestDto> dtoList) {
        return ResponseEntity.ok(fieldService.createBulk(dtoList));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<FieldDto>>> getAllFields(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(fieldService.getAllFields(page, size));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteField(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(fieldService.deleteById(id));
    }

}