package com.photon.endpoint.service;

import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.dto.*;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.endpoint.enums.BaseType;
import com.photon.endpoint.utils.ModelIntrospector;
import com.photon.endpoint.utils.ResponseModelTypeResolver;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.properties.ApplicationConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
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
                    Set<FeatureInfoDto> features = new LinkedHashSet<>();
                    Set<Class<?>> seenDtoClasses = new HashSet<>();
                    Set<ModelDescriptionDto> dtoDescriptions = new LinkedHashSet<>();

                    for (Class<?> clazz : annotatedClasses) {

                        FeatureInfo featureInfo = clazz.getAnnotation(FeatureInfo.class);
                        if (featureInfo == null) continue;

                        String basePath = "";
                        if (clazz.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping rm = clazz.getAnnotation(RequestMapping.class);
                            if (rm.value().length > 0) {
                                basePath = rm.value()[0];
                            }
                        }

                        Set<ActionInfoDto> actions = new LinkedHashSet<>();

                        for (Method method : clazz.getMethods()) {

                            if (!method.isAnnotationPresent(ActionInfo.class)) continue;
                            ActionInfo actionInfo = method.getAnnotation(ActionInfo.class);

                            // ---------- Request Body ----------
                            ActionModelDto requestBody = null;
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestBody.class)) {
                                    requestBody = new ActionModelDto();
                                    Type paramType = parameter.getParameterizedType();
                                    ResponseModelTypeResolver.Result reqResult =
                                            ResponseModelTypeResolver.resolve(paramType);

                                    Class<?> reqDto = reqResult.getModelClass();
                                    if (reqDto != null) {
                                        requestBody.setKey(parameter.getName());
                                        requestBody.setModelId(reqDto.getCanonicalName());
                                        requestBody.setCollection(reqResult.isCollection());

                                        if (seenDtoClasses.add(reqDto)) {
                                            dtoDescriptions.addAll(
                                                    ModelIntrospector.buildDtoGraph(reqDto)
                                            );
                                        }
                                    }
                                    break;
                                }
                            }

                            // ---------- Request Params ----------
                            Set<ActionParamDto> requestParams = new LinkedHashSet<>();
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestParam.class)) {
                                    RequestParam rp = parameter.getAnnotation(RequestParam.class);

                                    ActionParamDto param = new ActionParamDto();
                                    param.setKey(resolveRequestParamKey(parameter));
                                    param.setType(resolveBaseType(parameter));
                                    param.setCollection(Collection.class.isAssignableFrom(parameter.getType()));
                                    param.setRequired(rp.required());

                                    requestParams.add(param);
                                }
                            }

                            // ---------- Multipart ----------
                            Set<ActionMultipartDto> multipart = new LinkedHashSet<>();
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestPart.class)) {
                                    ResponseModelTypeResolver.Result reqResult =
                                            ResponseModelTypeResolver.resolve(parameter.getParameterizedType());

                                    ActionMultipartDto mp = new ActionMultipartDto();
                                    mp.setKey(parameter.getName());
                                    mp.setCollection(reqResult.isCollection());

                                    multipart.add(mp);
                                }
                            }

                            // ---------- Response Body ----------
                            ActionModelDto responseBody = null;
                            ResponseModelTypeResolver.Result resResult =
                                    ResponseModelTypeResolver.resolve(method.getGenericReturnType());

                            if (resResult.getModelClass() != null) {
                                responseBody = new ActionModelDto();
                                responseBody.setModelId(resResult.getModelClass().getCanonicalName());
                                responseBody.setCollection(resResult.isCollection());

                                if (seenDtoClasses.add(resResult.getModelClass())) {
                                    dtoDescriptions.addAll(
                                            ModelIntrospector.buildDtoGraph(resResult.getModelClass())
                                    );
                                }
                            }

                            // ---------- Mapping ----------
                            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mappings.entrySet()) {

                                if (!entry.getValue().getMethod().equals(method)) continue;

                                RequestMappingInfo info = entry.getKey();

                                String fullPath = info.getPatternsCondition()
                                        .getPatterns()
                                        .stream()
                                        .map(PathPattern::getPatternString)
                                        .findFirst()
                                        .orElse(basePath);

                                RequestMethod httpMethod = info.getMethodsCondition()
                                        .getMethods()
                                        .stream()
                                        .findFirst()
                                        .orElse(RequestMethod.GET);

                                actions.add(ActionInfoDto.builder()
                                        .actionId(actionInfo.id())
                                        .name(actionInfo.name())
                                        .description(actionInfo.description())
                                        .featureId(featureInfo.id())
                                        .path(fullPath)
                                        .requestMethod(httpMethod)
                                        .accessLevel(
                                                featureInfo.accessLevel().equals(AccessLevel.NONE)
                                                        ? actionInfo.accessLevel()
                                                        : featureInfo.accessLevel()
                                        )
                                        .securityLevel(actionInfo.securityLevel())
                                        .requestModel(requestBody)
                                        .requestParams(requestParams)
                                        .requestMultipart(multipart)
                                        .responseModel(responseBody)
                                        .operationName(method.getName())
                                        .build());
                            }
                        }

                        features.add(FeatureInfoDto.builder()
                                .featureId(featureInfo.id())
                                .name(featureInfo.name())
                                .moduleName(clazz.getSimpleName())
                                .path(basePath)
                                .description(featureInfo.description())
                                .actions(actions)
                                .build());
                    }

                    endpointDetails.setId(applicationConfigProperties.getApplicationName());
                    endpointDetails.setName(applicationConfigProperties.getApplicationName());
                    endpointDetails.setClientId(applicationConfigProperties.getXApiKey());
                    endpointDetails.setClientSecret(applicationConfigProperties.getXApiSecret());
                    endpointDetails.setFeatures(features);
                    endpointDetails.setModels(dtoDescriptions);

                    return ApiResponseDto.<EndpointDetailsDto>builder()
                            .success(true)
                            .opStatus(0)
                            .responseData(endpointDetails)
                            .build();

                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e ->
                        new ApplicationException(
                                ExceptionEnum.ERR_1009.getErrorResponseBody(e.getMessage()),
                                HttpStatus.OK
                        )
                );
    }

    // ---------- Helpers (same as servlet) ----------

    private BaseType resolveBaseType(Parameter parameter) {
        Class<?> type = parameter.getType();

        if (Collection.class.isAssignableFrom(type)) {
            return Set.class.isAssignableFrom(type) ? BaseType.SET : BaseType.LIST;
        }
        if (Map.class.isAssignableFrom(type)) return BaseType.MAP;
        if (type == String.class) return BaseType.STRING;
        if (type == int.class || type == Integer.class) return BaseType.INTEGER;
        if (type == long.class || type == Long.class) return BaseType.LONG;
        if (type == boolean.class || type == Boolean.class) return BaseType.BOOLEAN;
        if (type == float.class || type == Float.class) return BaseType.FLOAT;
        if (type == double.class || type == Double.class) return BaseType.DOUBLE;

        if (Date.class.isAssignableFrom(type) || type.getName().startsWith("java.time")) {
            return BaseType.DATE;
        }

        if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
            return BaseType.OBJECT;
        }

        return BaseType.UNKNOWN;
    }

    private String resolveRequestParamKey(Parameter parameter) {
        RequestParam rp = parameter.getAnnotation(RequestParam.class);
        if (rp == null) return parameter.getName();
        if (!rp.name().isEmpty()) return rp.name();
        if (!rp.value().isEmpty()) return rp.value();
        return parameter.getName();
    }
}