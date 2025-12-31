package com.photon.console.service;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.photon.auth.enums.AuthFilter;
import com.photon.console.entity.*;
import com.photon.console.gateway.dto.GatewayRoutRequestDto;
import com.photon.console.dto.request.UpdateActionInfoRequestDto;
import com.photon.console.entity.mapper.ActionInfoMapper;
import com.photon.console.entity.mapper.EndpointDetailsMapper;
import com.photon.console.entity.mapper.FeatureInfoMapper;
import com.photon.console.gateway.service.GatewayRouteService;
import com.photon.console.identity.service.RoleService;
import com.photon.console.repository.ActionInfoRepository;
import com.photon.console.repository.EndpointDetailsRepository;
import com.photon.console.repository.FeatureInfoRepository;
import com.photon.console.repository.GatewayFeatureActionDataSyncRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.request.RoleFeatureActionRequestDto;
import com.photon.endpoint.dto.ActionInfoDto;
import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.dto.FeatureInfoDto;
import com.photon.endpoint.dto.ModelDescriptionDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SecurityLevel;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.properties.ApplicationConfigProperties;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiManagerService {

    private static final String CHANNEL_CMD = "gateway:refresh:cmd";

    private final EurekaClient registry;
    private final RestTemplate restTemplate;
    private final ActionInfoRepository actionInfoRepository;
    private final FeatureInfoRepository featureInfoRepository;
    private final EndpointDetailsRepository endpointDetailsRepository;
    private final RoleService roleService;
    private final GatewayRouteService gatewayRouteService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ApplicationConfigProperties applicationConfigProperties;
    private final GatewayFeatureActionDataSyncRepository gatewayFeatureActionDataSyncRepository;;

    public ApiManagerService(EurekaClient registry, RestTemplate restTemplate, ActionInfoRepository actionInfoRepository,
                             FeatureInfoRepository featureInfoRepository, EndpointDetailsRepository endpointDetailsRepository,
                             RoleService roleService, GatewayRouteService gatewayRouteService, StringRedisTemplate stringRedisTemplate,
                             ApplicationConfigProperties applicationConfigProperties, GatewayFeatureActionDataSyncRepository gatewayFeatureActionDataSyncRepository) {
        this.registry = registry;
        this.restTemplate = restTemplate;
        this.actionInfoRepository = actionInfoRepository;
        this.featureInfoRepository = featureInfoRepository;
        this.endpointDetailsRepository = endpointDetailsRepository;
        this.roleService = roleService;
        this.gatewayRouteService = gatewayRouteService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.applicationConfigProperties = applicationConfigProperties;
        this.gatewayFeatureActionDataSyncRepository = gatewayFeatureActionDataSyncRepository;
    }

    @Transactional
    public ApiResponseDto<List<EndpointDetailsDto>> getRegisteredServices(List<String> services) throws ApplicationException {
        try{
            log.debug("getRegisteredServices input params: {}",services.toString());
            List<EndpointDetails> endpointDetails = this.endpointDetailsRepository.findAllById(services);

            return SuccessEnum.SUCCESS.getSuccessResponseBody(endpointDetails.stream().map(EndpointDetailsMapper::toDto).collect(Collectors.toList()));
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getRegisteredServices: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in getRegisteredServices: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<String>> getAllApps() {
        List<String> services = this.endpointDetailsRepository.findAllIds();
        services.removeAll(List.of("API-GATEWAY", "API-CONFIG", "CONSOLE", "ALERT"));
        return SuccessEnum.SUCCESS.getSuccessResponseBody(services);
    }

    public ApiResponseDto<List<String>> getAppsForLogging() {
        List<String> services = this.registry.getApplications()
                .getRegisteredApplications()
                .stream()
                .map(Application::getName)
                .collect(Collectors.toList());
        return SuccessEnum.SUCCESS.getSuccessResponseBody(services);
    }

    public ApiResponseDto<ActionInfoDto> getActionById(UUID id) {
        ActionInfoDto actionInfoDto = this.actionInfoRepository.findById(id)
                .map(ActionInfoMapper::toDto)
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND));

        return SuccessEnum.SUCCESS.getSuccessResponseBody(actionInfoDto);
    }

    public ApiResponseDto<?> updateAllAction(List<UpdateActionInfoRequestDto> updateActionInfoRequestList){
        try {

            CompletableFuture.runAsync(() -> updateActionInfoRequestList.forEach(updateActionInfoRequestDto -> {
                try {
                    this.updateAction(updateActionInfoRequestDto.getId(), updateActionInfoRequestDto.getActionInfo());
                } catch (Exception e) {
                    log.error("Failed to update action for ID {}: {}", updateActionInfoRequestDto.getId(), e.getMessage(), e);
                }
            }));
            return SuccessEnum.UPDATED.getSuccessResponseBody();

        } catch (ApplicationException ae) {
            log.error("ApplicationException in updateAllAction: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in updateAllAction: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> updateAction(UUID id, ActionInfoDto actionInfoDto) throws ApplicationException {
        try {
            String addedRoutId = null;
            String removedRoutId = null;
            ActionInfo action = this.actionInfoRepository.findById(id).orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND));

            actionInfoDto.setFeatureId(action.getFeatureId());
            actionInfoDto.setActionId(action.getActionId());

            Map<String, Map<Long, String>> added = new HashMap<>();
            Map<String, Map<Long, String>> removed = new HashMap<>();
            boolean isModified = false;

            if (Objects.nonNull(actionInfoDto.getSecurityLevel()) && !actionInfoDto.getSecurityLevel().equals(SecurityLevel.PRIVATE)) {

                action.setSecurityLevel(actionInfoDto.getSecurityLevel());

                String routId = actionInfoDto.getFeatureId().concat("_").concat(action.getActionId());
                String path = action.getPath();
                addedRoutId = routId;

                GatewayRoutRequestDto gatewayRoutRequest = new GatewayRoutRequestDto();
                gatewayRoutRequest.setRouteId(routId);
                gatewayRoutRequest.setPath(path);
                gatewayRoutRequest.setAuthFilter(AuthFilter.valueOf(actionInfoDto.getSecurityLevel().name()));
                gatewayRoutRequest.setIdps(actionInfoDto.getUserRoles() != null ? actionInfoDto.getUserRoles().keySet() : Collections.emptySet());
                gatewayRoutRequest.setRoles(actionInfoDto.getUserRoles() != null ? actionInfoDto.getUserRoles().values().stream().flatMap(longStringMap -> longStringMap.values().stream()).collect(Collectors.toSet()) : Collections.emptySet());
                gatewayRoutRequest.setMethod(action.getRequestMethod().name());
                gatewayRoutRequest.setApplicationId(action.getFeature().getEndpointDetails().getId());

                ApiResponseDto<?> response = this.gatewayRouteService.addOrUpdatePath(routId, gatewayRoutRequest);

                if (response == null || !response.isSuccess())
                    throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(response != null ? response.getMessage() : "Error during endpoint addition in gateway"), HttpStatus.BAD_REQUEST);

                isModified = true;

            } else if (Objects.nonNull(actionInfoDto.getSecurityLevel()) && actionInfoDto.getSecurityLevel().equals(SecurityLevel.PRIVATE)) {

                action.setSecurityLevel(actionInfoDto.getSecurityLevel());
                String routId = actionInfoDto.getFeatureId().concat("_").concat(action.getActionId());
                removedRoutId = routId;
                ApiResponseDto<?> response = this.gatewayRouteService.deletePath(routId);

                if (response == null || !response.isSuccess())
                    throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(response != null ? response.getMessage() : "Error during endpoint delete from gateway"), HttpStatus.BAD_REQUEST);

                isModified = true;
            }

            Map<String, Map<Long, String>> currentRoles;
            Map<String, Map<Long, String>> newRoles;

            if ((Objects.isNull(action.getUserRoles()) || action.getUserRoles().isEmpty()) && (Objects.nonNull(actionInfoDto.getUserRoles()) && !actionInfoDto.getUserRoles().isEmpty())) {

                action.setUserRoles(actionInfoDto.getUserRoles());
                added = actionInfoDto.getUserRoles();
                isModified = true;

            } else if ((Objects.nonNull(action.getUserRoles()) && !action.getUserRoles().isEmpty()) && (Objects.nonNull(actionInfoDto.getUserRoles()) && !actionInfoDto.getUserRoles().isEmpty())) {

                currentRoles = action.getUserRoles();
                newRoles = actionInfoDto.getUserRoles();

                Map<String, Map<Long, String>> addedRoles = new HashMap<>(newRoles);
                currentRoles.keySet().forEach(addedRoles::remove);
                if (!addedRoles.isEmpty()) {
                    added.putAll(addedRoles);
                    action.getUserRoles().putAll(addedRoles);
                    isModified = true;
                }

                Map<String, Map<Long, String>> removedRoles = new HashMap<>(currentRoles);
                newRoles.keySet().forEach(removedRoles::remove);
                if (!removedRoles.isEmpty()) {
                    removed.putAll(removedRoles);
                    removedRoles.keySet().forEach(action.getUserRoles()::remove);
                    isModified = true;
                }
                action.getUserRoles().putAll(newRoles);

            } else if ((Objects.nonNull(action.getUserRoles()) && !action.getUserRoles().isEmpty()) && (Objects.isNull(actionInfoDto.getUserRoles()) || actionInfoDto.getUserRoles().isEmpty())) {
                removed.putAll(action.getUserRoles());
                action.setUserRoles(Collections.emptyMap());
                isModified = true;
            }

            if (isModified)
                actionInfoRepository.save(action);
            else
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody("No changes found in request payload"), HttpStatus.BAD_REQUEST);

            handleRoleFeatureUpdates(added, removed, actionInfoDto, action.getFeature().getEndpointDetails().getId());

            if(!StringUtils.isBlank(removedRoutId))
                this.triggerRefreshRemovedRoute(removedRoutId);
            if(!StringUtils.isBlank(addedRoutId))
                this.triggerRefreshAddedRoute(addedRoutId);

            return SuccessEnum.UPDATED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in updateAction for ID {}: {}", id, ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in updateAction for ID {}: {}", id, e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRoleFeatureUpdates(Map<String, Map<Long, String>> added, Map<String, Map<Long, String>> removed, ActionInfoDto actionInfoDto, String appId) throws ApplicationException {

        if (added != null && !added.isEmpty()) {
            try {
                List<RoleFeatureActionRequestDto> addedRequests = prepareRoleFeatureActionRequest(actionInfoDto, added, appId);
                ApiResponseDto<?> response = this.roleService.addAllFeatureActionInRole(addedRequests);
                if (response == null || !response.isSuccess()) {
                    throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(response != null ? response.getMessage() : "Error during feature action addition"), HttpStatus.BAD_REQUEST);
                }
            } catch (ApplicationException ae) {
                log.error("ApplicationException in adding feature actions: {}", ae.getMessage(), ae);
                throw ae;
            } catch (Exception e) {
                log.error("Exception in adding feature actions: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (removed != null && !removed.isEmpty()) {
            try {
                List<RoleFeatureActionRequestDto> removedRequests = prepareRoleFeatureActionRequest(actionInfoDto, removed, appId);
                ApiResponseDto<?> response = this.roleService.deleteAllFeatureActionInRole(removedRequests);
                if (response == null || !response.isSuccess()) {
                    throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(response != null ? response.getMessage() : "Error during feature action removal"), HttpStatus.BAD_REQUEST);
                }
            } catch (ApplicationException ae) {
                log.error("ApplicationException in removing feature actions: {}", ae.getMessage(), ae);
                throw ae;
            } catch (Exception e) {
                log.error("Exception in removing feature actions: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private List<RoleFeatureActionRequestDto> prepareRoleFeatureActionRequest(ActionInfoDto actionInfoDto, Map<String,Map<Long, String>> added, String appId) {
        List<RoleFeatureActionRequestDto> roleFeatureActionReqList = new ArrayList<>();

        added.forEach((k, v)->{
            v.keySet().forEach(id -> {
                String featureAction = actionInfoDto.getFeatureId() + ":" + actionInfoDto.getActionId();

                Map<String, Set<String>> featureActions = new HashMap<>();
                featureActions.put(appId, Set.of(featureAction));

                RoleFeatureActionRequestDto roleFeatureActionRequest = RoleFeatureActionRequestDto.builder()
                        .id(id)
                        .featureActions(featureActions)
                        .build();
                roleFeatureActionReqList.add(roleFeatureActionRequest);

            });
        });

        return roleFeatureActionReqList;
    }


    @Async
    @Transactional
    public void synchronizeAllFeatureAction(String applicationId, EndpointDetailsDto endpointDetailsDto) {

        Map<String, FeatureInfoDto> featureMap = endpointDetailsDto.getFeatures()
                .stream()
                .collect(Collectors.toMap(FeatureInfoDto::getFeatureId, featureInfoDto -> featureInfoDto));

        Optional<EndpointDetails> existingEndpointDetailsOptional = this.endpointDetailsRepository.findById(applicationId);

        if (existingEndpointDetailsOptional.isPresent()) {
            EndpointDetails existingEndpointDetails = existingEndpointDetailsOptional.get();

            Set<FeatureInfo> existingFeatures = existingEndpointDetails.getFeatures();
            Set<FeatureInfo> featuresToRemove = new HashSet<>(existingFeatures);
            Set<String> modelIdsToRemove = new HashSet<>();

            for (FeatureInfo existingFeature : existingFeatures) {
                if (featureMap.containsKey(existingFeature.getFeatureId())) {
                    String featureId = existingFeature.getFeatureId();
                    FeatureInfoDto featureInfoDto = featureMap.get(featureId);

                    if (!existingFeature.getName().equals(featureInfoDto.getName()) ||
                            !existingFeature.getDescription().equals(featureInfoDto.getDescription()) ||
                            !existingFeature.getPath().equals(featureInfoDto.getPath())) {
                        existingFeature.setName(featureInfoDto.getName());
                        existingFeature.setDescription(featureInfoDto.getDescription());
                        existingFeature.setPath(featureInfoDto.getPath());
                    }

                    Map<String, ActionInfoDto> actionMap = featureInfoDto.getActions().stream()
                            .collect(Collectors.toMap(ActionInfoDto::getActionId, actionInfoDto -> actionInfoDto));

                    Set<ActionInfo> existingActions = existingFeature.getActions();
                    Set<ActionInfo> actionsToRemove = new HashSet<>(existingActions);

                    for (ActionInfo existingAction : existingActions) {
                        if (actionMap.containsKey(existingAction.getActionId())) {
                            ActionInfoDto actionInfoDto = actionMap.get(existingAction.getActionId());

                            //if (!actionInfoDto.equals(ActionInfoMapper.toDto(existingAction))) {
                                existingAction.setActionId(actionInfoDto.getActionId());
                                existingAction.setDescription(actionInfoDto.getDescription());
                                existingAction.setName(actionInfoDto.getName());
                                existingAction.setPath(actionInfoDto.getPath());
                                existingAction.setRequestMethod(actionInfoDto.getRequestMethod());
                                existingAction.setRequestBodyModelId(actionInfoDto.getRequestBodyModelId());
                                existingAction.setResponseBodyModelId(actionInfoDto.getResponseBodyModelId());
                                existingAction.setIsRequestBodyCollection(actionInfoDto.isRequestBodyCollection());
                                existingAction.setIsResponseBodyCollection(actionInfoDto.isResponseBodyCollection());
                           // }

                            actionsToRemove.remove(existingAction);
                            actionMap.remove(existingAction.getActionId());
                        }
                    }

                    actionsToRemove.forEach(existingActions::remove);

                    actionsToRemove.forEach(actionInfo -> {
                        if(!StringUtils.isEmpty(actionInfo.getRequestBodyModelId()))
                            modelIdsToRemove.add(actionInfo.getRequestBodyModelId());
                        if(!StringUtils.isEmpty(actionInfo.getResponseBodyModelId()))
                            modelIdsToRemove.add(actionInfo.getResponseBodyModelId());

                        String routId = actionInfo.getFeatureId().concat("_").concat(actionInfo.getActionId());
                        String roleFeatureAction = actionInfo.getFeatureId().concat(":").concat(actionInfo.getActionId());
                        GatewayFeatureActionDataSync gatewayFeatureActionDataSync = new GatewayFeatureActionDataSync();
                        gatewayFeatureActionDataSync.setGatewayRouteId(routId);
                        gatewayFeatureActionDataSync.setRoleFeatureAction(roleFeatureAction);
                        this.gatewayFeatureActionDataSyncRepository.save(gatewayFeatureActionDataSync);
                    });

                    actionMap.forEach((actionId, newActionInfoDto) -> {
                        ActionInfo newAction = ActionInfoMapper.toEntity(newActionInfoDto);
                        newAction.setFeature(existingFeature);
                        existingActions.add(newAction);
                    });

                    featureMap.remove(existingFeature.getFeatureId());
                    featuresToRemove.remove(existingFeature);
                }
            }

            featuresToRemove.forEach(existingFeatures::remove);

            featureMap.forEach((featureId, featureInfoDto) -> {
                FeatureInfo newFeature = FeatureInfoMapper.toEntity(featureInfoDto);
                newFeature.setEndpointDetails(existingEndpointDetails);
                existingFeatures.add(newFeature);
                log.info("Feature with ID {} not found, creating a new feature", featureId);
            });

            Map<String, Model> existingModelMap = existingEndpointDetails.getModels().stream().collect(Collectors.toMap(Model::getModelId, model -> model));
            Set<ModelDescriptionDto> modelDtos = Optional.ofNullable(endpointDetailsDto.getModels()).orElse(Collections.emptySet());

            Set<Model> newModelEntities = new HashSet<>();

            //modelIdsToRemove.forEach(existingModelMap::remove);

            for (ModelDescriptionDto dto : modelDtos) {
                if (!existingModelMap.containsKey(dto.getId())) {
                    Model model = new Model();
                    model.setId(dto.getId().concat("_").concat(endpointDetailsDto.getId()));
                    model.setModelId(dto.getId());
                    model.setName(dto.getName());

                    Set<Map<String, String>> fieldSet = dto.getFields().stream().map(field -> {
                        Map<String, String> fieldMap = new HashMap<>();
                        fieldMap.put("name", field.getName());
                        fieldMap.put("type", field.getType().name());
                        if (field.getReferenceType() != null) {
                            fieldMap.put("referenceType", field.getReferenceType());
                        }
                        return fieldMap;
                    }).collect(Collectors.toSet());

                    model.setFields(fieldSet);
                    model.setEndpointDetails(existingEndpointDetails);
                    newModelEntities.add(model);
                }
            }

            Set<Model> existingModels = existingEndpointDetails.getModels();
            existingModels.removeIf(existing ->
                    newModelEntities.stream().noneMatch(n -> n.getModelId().equals(existing.getModelId()))
            );
            newModelEntities.forEach(model -> {
                if (!existingModels.contains(model)) {
                    existingModels.add(model);
                }
            });
            existingEndpointDetails.setModels(existingModels);

            this.endpointDetailsRepository.save(existingEndpointDetails);

        } else {
            EndpointDetails newEndpoint = EndpointDetailsMapper.toEntity(endpointDetailsDto);

            Set<Model> modelEntities = Optional.ofNullable(endpointDetailsDto.getModels()).orElse(Collections.emptySet()).stream().map(dto -> {
                Model model = new Model();
                model.setId(dto.getId().concat("_").concat(endpointDetailsDto.getId()));
                model.setModelId(dto.getId());
                model.setName(dto.getName());

                Set<Map<String, String>> fields = dto.getFields().stream().map(field -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", field.getName());
                    map.put("type", field.getType().name());
                    if (field.getReferenceType() != null) {
                        map.put("referenceType", field.getReferenceType());
                    }
                    return map;
                }).collect(Collectors.toSet());

                model.setEndpointDetails(newEndpoint);
                model.setFields(fields);
                return model;
            }).collect(Collectors.toSet());

            newEndpoint.setModels(modelEntities);
            this.endpointDetailsRepository.save(newEndpoint);
        }
    }

    public ApiResponseDto<List<FeatureInfoDto>> getFeatureActions(String applicationId, int pageNumber, int pageSize) {
        try{
            log.debug("Getting feature actions for {}", applicationId);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<FeatureInfoDto> featureInfoPage = this.featureInfoRepository.findByEndpointDetails_Id(pageable, applicationId).map(FeatureInfoMapper::toDto);
            List<FeatureInfoDto> featureInfoList = featureInfoPage.stream().toList();

            return SuccessEnum.SUCCESS.getSuccessResponseBody(featureInfoList)
                    .page(pageable.getPageNumber()).size(featureInfoList.size())
                    .totalPages(featureInfoPage.getTotalPages()).totalRecords((int) featureInfoPage.getTotalElements());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getFeatureActions: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getFeatureActions: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    public void triggerRefreshAddedRoute(String routeId) {
        String ts = String.valueOf(Instant.now().toEpochMilli());
        this.stringRedisTemplate.convertAndSend(CHANNEL_CMD, "REFRESH:ONE:ADDED:" + routeId + ":" + ts);
    }

    @Async
    public void triggerRefreshRemovedRoute(String routeId) {
        String ts = String.valueOf(Instant.now().toEpochMilli());
        this.stringRedisTemplate.convertAndSend(CHANNEL_CMD, "REFRESH:ONE:REMOVED:" + routeId + ":" + ts);
    }

    public ApiResponseDto<?> applicationHealth(String applicationId) {
        try {

            String url = "lb://" + applicationId + "/actuator/health";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String username = this.applicationConfigProperties.getCompositeXApiKey();
            String password = this.applicationConfigProperties.getCompositeXApiSecret();

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);

            headers.set("Authorization", authHeader);

            HttpEntity<?> entity = new HttpEntity<>(null, headers);
            ResponseEntity<?> response = this.restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            log.warn("Register endpoints response : {} ", Objects.requireNonNull(response.getBody()));

            if (response.getStatusCode().value() == 200) {
                log.info("Health check passed on applicationId {}", applicationId);
                return SuccessEnum.SUCCESS.getSuccessResponseBody(response.getBody());
            } else {
                Map<String, String> resMap = new HashMap<>();
                resMap.put("status", "UNKNOWN");
                return SuccessEnum.SUCCESS.getSuccessResponseBody(resMap);
            }
        } catch (Exception ex) {
          log.error("Error while checking the application health : {}", ex.getMessage());
            Map<String, String> resMap = new HashMap<>();
            resMap.put("status", "SERVICE_UNAVAILABLE");
            return SuccessEnum.SUCCESS.getSuccessResponseBody(resMap);
        }
    }

}