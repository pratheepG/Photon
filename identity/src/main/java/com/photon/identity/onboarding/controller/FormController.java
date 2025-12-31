package com.photon.identity.onboarding.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.onboarding.dto.request.FormRequestDto;
import com.photon.identity.onboarding.dto.response.FormListItemDto;
import com.photon.identity.onboarding.dto.response.FormPreviewDto;
import com.photon.identity.onboarding.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding/form")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody FormRequestDto dto) {
        return ResponseEntity.ok(formService.createForm(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> patchForm(@PathVariable Long id, @RequestBody FormRequestDto dto) {
        return ResponseEntity.ok(formService.patchForm(id, dto));
    }

    //    @PostMapping("/bulk")
//    public ResponseEntity<ApiResponseDto<?>> createForms(@RequestBody List<FormRequestDto> forms) {
//        return ResponseEntity.ok(formService.createBulk(forms));
//    }
//
//    @PutMapping
//    public ResponseEntity<ApiResponseDto<?>> update(@RequestBody FormRequestDto dto) {
//        return ResponseEntity.ok(formService.updateForm(dto));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponseDto<?>> delete(@PathVariable String id) {
//        return ResponseEntity.ok(formService.deleteForm(id));
//    }
//
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<FormPreviewDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(formService.getFormPreview(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<FormListItemDto>>> getAllByPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(formService.getAllForms(page, size));
    }
}