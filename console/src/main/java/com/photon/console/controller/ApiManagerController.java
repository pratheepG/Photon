package com.photon.console.controller;

import com.photon.console.dto.request.UpdateActionInfoRequestDto;
import com.photon.console.service.ApiManagerService;
import com.photon.console.service.ClientSdkService;
import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.dto.ActionInfoDto;
import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.dto.FeatureInfoDto;
import com.photon.enums.SuccessEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api-manager")
public class ApiManagerController {

    private final ApiManagerService apiManagerService;
    private final ClientSdkService clientSdkService;

    public ApiManagerController(ApiManagerService apiManagerService, ClientSdkService clientSdkService) {
        this.apiManagerService = apiManagerService;
        this.clientSdkService = clientSdkService;
    }

    @PutMapping(path = "/services/{applicationId}")
    public ResponseEntity<ApiResponseDto<?>> registerEndpoint(@RequestBody EndpointDetailsDto endpointDetailsDto, @PathVariable String applicationId) {
        this.apiManagerService.synchronizeAllFeatureAction(applicationId, endpointDetailsDto);
        return ResponseEntity.accepted().body(SuccessEnum.SUCCESS.getSuccessResponseBody());
    }

    @GetMapping(path = "/apps")
    public ResponseEntity<ApiResponseDto<List<String>>> getAllApplication(){
        return ResponseEntity.ok().body(this.apiManagerService.getAllApps());
    }

    @GetMapping(path = "/logging-apps")
    public ResponseEntity<ApiResponseDto<List<String>>> getAppsForLogging(){
        return ResponseEntity.ok().body(this.apiManagerService.getAppsForLogging());
    }

    @GetMapping(path = "/services/{applicationId}")
    public ResponseEntity<ApiResponseDto<List<EndpointDetailsDto>>> getAllServicesByApplicationId(@PathVariable String applicationId){
        List<String> applicationIds = new ArrayList<>();
        applicationIds.add(applicationId);
        return ResponseEntity.ok().body(this.apiManagerService.getRegisteredServices(applicationIds));
    }

    @GetMapping(path = "/services/feature/{applicationId}")
    public ResponseEntity<ApiResponseDto<List<FeatureInfoDto>>> getAllFeatureActionByApplicationId(@PathVariable String applicationId, @RequestParam(required = false) int pageNumber, int pageSize){
        return ResponseEntity.ok().body(this.apiManagerService.getFeatureActions(applicationId, pageNumber, pageSize));
    }

    @GetMapping(path = "/services/action/{id}")
    public ResponseEntity<ApiResponseDto<ActionInfoDto>> getActionById(@PathVariable @NotNull(message = "ID cannot be null") UUID id){
        return ResponseEntity.ok().body(this.apiManagerService.getActionById(id));
    }

    @PatchMapping(path = "/services/action/{id}")
    public ResponseEntity<ApiResponseDto<?>> updateAction(@PathVariable @NotNull(message = "ID cannot be null") UUID id, @RequestBody ActionInfoDto actionInfoDto){
        return ResponseEntity.ok().body(this.apiManagerService.updateAction(id, actionInfoDto));
    }

    @PatchMapping(path = "/services/action")
    public ResponseEntity<ApiResponseDto<?>> updateAllAction(@RequestBody List<UpdateActionInfoRequestDto> updateActionInfoRequestList){
        return ResponseEntity.ok().body(this.apiManagerService.updateAllAction(updateActionInfoRequestList));
    }

    @GetMapping(path = "/sdk/generate")
    public SseEmitter generateSdk(@RequestParam String languagesCsv){
        return this.clientSdkService.generateSdk(languagesCsv);
    }

    @GetMapping(path = "/sdk/download")
    public ResponseEntity<ByteArrayResource> downloadSdk(@RequestParam UUID id){
        return this.clientSdkService.getSdkById(id);
    }

    @GetMapping(path = "/{applicationId}/health")
    public ResponseEntity<ApiResponseDto<?>> getApplicationHealth(@PathVariable @NotNull(message = "applicationId cannot be null") String applicationId){
        return ResponseEntity.ok().body(this.apiManagerService.applicationHealth(applicationId));
    }

}