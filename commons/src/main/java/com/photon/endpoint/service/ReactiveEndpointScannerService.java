package com.photon.endpoint.service;

import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.dto.ActionInfoDto;
import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.dto.FeatureInfoDto;
import com.photon.endpoint.dto.ModelDescriptionDto;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.endpoint.utils.ModelIntrospector;
import com.photon.endpoint.utils.ResponseModelTypeResolver;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.properties.ApplicationConfigProperties;
import com.photon.utils.PhotonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveEndpointScannerService {

    private final ApplicationConfigProperties applicationConfigProperties;
    private final ApplicationContext applicationContext;
    private final RequestMappingHandlerMapping handlerMapping;

    public ReactiveEndpointScannerService(ApplicationConfigProperties applicationConfigProperties,
                                          ApplicationContext applicationContext,
                                          RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.applicationConfigProperties = applicationConfigProperties;
        this.applicationContext = applicationContext;
        this.handlerMapping = requestMappingHandlerMapping;
    }

    public Mono<ApiResponseDto<EndpointDetailsDto>> scanEndpoints() {
        return Mono.fromCallable(() -> {

                    Map<RequestMappingInfo, HandlerMethod> mappings = handlerMapping.getHandlerMethods();
                    String[] beanNames = applicationContext.getBeanNamesForAnnotation(FeatureInfo.class);

                    Set<Class<?>> annotatedClasses = Arrays.stream(beanNames)
                            .map(applicationContext::getType)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    EndpointDetailsDto endpointDetails = new EndpointDetailsDto();
                    Set<FeatureInfoDto> features = new HashSet<>();
                    Set<Class<?>> seenDtoClasses = new HashSet<>();
                    Set<ModelDescriptionDto> dtoDescriptions = new HashSet<>();

                    for (Class<?> clazz : annotatedClasses) {
                        FeatureInfo featureInfo = PhotonUtils.requireNonNull(clazz.getAnnotation(FeatureInfo.class));
                        String basePath = "";

                        if (clazz.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping classMapping = PhotonUtils.requireNonNull(clazz.getAnnotation(RequestMapping.class));
                            if (classMapping.value().length > 0) {
                                basePath = classMapping.value()[0];
                            }
                        }

                        Set<ActionInfoDto> actions = new HashSet<>();

                        for (Method method : clazz.getDeclaredMethods()) {
                            if (!method.isAnnotationPresent(ActionInfo.class)) continue;

                            ActionInfo actionInfo = PhotonUtils.requireNonNull(method.getAnnotation(ActionInfo.class));

                            // -- Request Body
                            String requestBodyModelId = null;
                            boolean isRequestBodyCollection = false;
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestBody.class)) {
                                    Type paramType = parameter.getParameterizedType();
                                    ResponseModelTypeResolver.Result reqResult = ResponseModelTypeResolver.resolve(paramType);
                                    Class<?> reqDto = reqResult.getModelClass();
                                    if (reqDto != null) {
                                        requestBodyModelId = reqDto.getCanonicalName();
                                        isRequestBodyCollection = reqResult.isCollection();
                                        if (seenDtoClasses.add(reqDto)) {
                                            dtoDescriptions.addAll(ModelIntrospector.buildDtoGraph(reqDto));
                                        }
                                    }
                                }
                            }

                            // -- Response Body
                            ResponseModelTypeResolver.Result resResult = ResponseModelTypeResolver.resolve(method.getGenericReturnType());
                            String responseBodyModelId = null;
                            boolean isResponseBodyCollection = false;
                            if (resResult.getModelClass() != null) {
                                responseBodyModelId = resResult.getModelClass().getCanonicalName();
                                isResponseBodyCollection = resResult.isCollection();
                                if (seenDtoClasses.add(resResult.getModelClass())) {
                                    dtoDescriptions.addAll(ModelIntrospector.buildDtoGraph(resResult.getModelClass()));
                                }
                                if (resResult.getCollectionDepth() > 1) {
                                    log.warn("Nested collection depth in return type of method '{}'", method.getName());
                                }
                            }

                            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mappings.entrySet()) {
                                HandlerMethod handler = entry.getValue();
                                if (!handler.getMethod().equals(method)) continue;

                                RequestMappingInfo mappingInfo = entry.getKey();
                                String fullPath = PhotonUtils.requireNonNull(mappingInfo.getPatternsCondition()).getPatterns().stream()
                                        .map(PathPattern::getPatternString)
                                        .findFirst()
                                        .orElse(basePath);
                                RequestMethod httpMethod = mappingInfo.getMethodsCondition().getMethods().stream()
                                        .findFirst().orElse(RequestMethod.GET);

                                actions.add(ActionInfoDto.builder()
                                        .actionId(actionInfo.id())
                                        .name(actionInfo.name())
                                        .description(actionInfo.description())
                                        .featureId(featureInfo.id())
                                        .path(fullPath)
                                        .requestMethod(httpMethod)
                                        .accessLevel((featureInfo.accessLevel().equals(AccessLevel.NONE))?actionInfo.accessLevel():featureInfo.accessLevel())
                                        .securityLevel(actionInfo.securityLevel())
                                        .requestBodyModelId(requestBodyModelId)
                                        .isRequestBodyCollection(isRequestBodyCollection)
                                        .responseBodyModelId(responseBodyModelId)
                                        .isResponseBodyCollection(isResponseBodyCollection)
                                        .operationName(method.getName())
                                        .build());
                            }
                        }

                        String moduleName = clazz.getSimpleName().replaceFirst("Controller$", "");

                        features.add(FeatureInfoDto.builder()
                                .featureId(featureInfo.id())
                                .name(featureInfo.name())
                                .moduleName(moduleName)
                                .path(basePath)
                                .description(featureInfo.description())
                                .actions(actions)
                                .build());
                    }

                    endpointDetails.setId(applicationConfigProperties.getApplicationName());
                    endpointDetails.setName(applicationConfigProperties.getApplicationName());
                    endpointDetails.setClientId(applicationConfigProperties.getXApiKey());
                    endpointDetails.setClientSecret(applicationConfigProperties.getXApiKey());
                    endpointDetails.setFeatures(features);
                    endpointDetails.setModels(dtoDescriptions);

                    return ApiResponseDto.<EndpointDetailsDto>builder()
                            .success(true)
                            .opStatus(0)
                            .responseData(endpointDetails)
                            .build();

                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("❌ Error scanning reactive endpoints: {}", e.getMessage(), e))
                .onErrorMap(e -> new ApplicationException(ExceptionEnum.ERR_1009.getErrorResponseBody(e.getMessage()), HttpStatus.OK));
    }
}