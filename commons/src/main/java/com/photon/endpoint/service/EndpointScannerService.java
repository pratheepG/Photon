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
import com.photon.utils.PhotonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class EndpointScannerService {

    private final ApplicationConfigProperties applicationConfigProperties;
    private final RequestMappingHandlerMapping handlerMapping;
    private final ApplicationContext applicationContext;

    public EndpointScannerService(ApplicationConfigProperties applicationConfigProperties,
                                  RequestMappingHandlerMapping requestMappingHandlerMapping,
                                  ApplicationContext applicationContext) {
        this.applicationConfigProperties = applicationConfigProperties;
        this.handlerMapping = requestMappingHandlerMapping;
        this.applicationContext = applicationContext;
    }

    public ApiResponseDto<EndpointDetailsDto> scanEndpoints() {
        try {
            Map<RequestMappingInfo, HandlerMethod> mappings = handlerMapping.getHandlerMethods();
            String[] beanNames = applicationContext.getBeanNamesForAnnotation(FeatureInfo.class);

            Set<Class<?>> annotatedClasses = Arrays.stream(beanNames)
                    .map(applicationContext::getType)
                    .peek(nameType -> {
                        // debug output for beans that might resolve to null type
                        // nothing to do here; peek is only for side-effects if needed
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            EndpointDetailsDto endpointDetails = new EndpointDetailsDto();
            Set<FeatureInfoDto> features = new HashSet<>();
            Set<Class<?>> seenDtoClasses = new HashSet<>();
            Set<ModelDescriptionDto> dtoDescriptions = new HashSet<>();

            for (Class<?> clazz : annotatedClasses) {
                try {
                    FeatureInfo featureInfo = clazz.getAnnotation(FeatureInfo.class);
                    if (featureInfo == null) {
                        log.warn("@FeatureInfo not present on class {} despite bean registration — skipping", clazz.getName());
                        continue;
                    }

                    String basePath = "";
                    RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
                    if (classMapping != null && classMapping.value().length > 0) {
                        basePath = classMapping.value()[0];
                    }

                    Set<ActionInfoDto> actions = new HashSet<>();

                    for (Method method : clazz.getDeclaredMethods()) {
                        try {
                            if (!method.isAnnotationPresent(ActionInfo.class)) {
                                continue;
                            }

                            ActionInfo actionInfo = method.getAnnotation(ActionInfo.class);
                            if (actionInfo == null) {
                                log.warn("Method {} in {} declared @ActionInfo but annotation returned null — skipping method", method.getName(), clazz.getName());
                                continue;
                            }

                            //-- Request body
                            ActionModelDto requestBody = null;
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestBody.class)) {
                                    requestBody = new ActionModelDto();
                                    Type paramType = parameter.getParameterizedType();
                                    ResponseModelTypeResolver.Result reqResult = ResponseModelTypeResolver.resolve(paramType);
                                    Class<?> reqDto = reqResult.getModelClass();
                                    if (reqDto != null) {
                                        requestBody.setModelId(reqDto.getCanonicalName());
                                        requestBody.setKey(parameter.getName());
                                        requestBody.setCollection(reqResult.isCollection());
                                        if (seenDtoClasses.add(reqDto)) {
                                            dtoDescriptions.addAll(ModelIntrospector.buildDtoGraph(reqDto));
                                        }
                                    }
                                    break;
                                }
                            }

                            //-- Request param
                            Set<ActionParamDto> requestParams = new HashSet<>();
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestParam.class)) {
                                    RequestParam rp = parameter.getAnnotation(RequestParam.class);
                                    Type paramType = parameter.getParameterizedType();
                                    ResponseModelTypeResolver.Result reqResult = ResponseModelTypeResolver.resolve(paramType);
                                    Class<?> reqDto = reqResult.getModelClass();
                                    if (reqDto != null) {
                                        ActionParamDto  requestParam = new ActionParamDto();
                                        requestParam.setType(resolveBaseType(parameter));
                                        requestParam.setKey(resolveRequestParamKey(parameter));
                                        requestParam.setCollection(reqResult.isCollection());
                                        requestParam.setRequired(rp.required());
                                        requestParams.add(requestParam);
                                    }
                                }
                            }

                            //-- Multipart request
                            Set<ActionMultipartDto> requestMultipartBodySet = new HashSet<>();
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(RequestPart.class)) {
                                    Type paramType = parameter.getParameterizedType();
                                    ResponseModelTypeResolver.Result reqResult = ResponseModelTypeResolver.resolve(paramType);
                                    Class<?> reqDto = reqResult.getModelClass();
                                    if (reqDto != null) {
                                        ActionMultipartDto requestMultipartBody = new ActionMultipartDto();
                                        requestMultipartBody.setKey(parameter.getName());
                                        requestMultipartBody.setCollection(reqResult.isCollection());
                                        requestMultipartBodySet.add(requestMultipartBody);
                                    }
                                }
                            }

                            //-- Response body
                            ResponseModelTypeResolver.Result resResult = ResponseModelTypeResolver.resolve(method.getGenericReturnType());
                            ActionModelDto responseBody = new ActionModelDto();
                            if (resResult.getModelClass() != null) {
                                responseBody.setCollection(resResult.isCollection());
                                responseBody.setModelId(resResult.getModelClass().getCanonicalName());
                                if (seenDtoClasses.add(resResult.getModelClass())) {
                                    dtoDescriptions.addAll(ModelIntrospector.buildDtoGraph(resResult.getModelClass()));
                                }

                                if (resResult.getCollectionDepth() > 1) {
                                    log.warn("Nested collection depth in return type of method '{}.{}'", clazz.getName(), method.getName());
                                }
                            }

                            // find matching handler mapping entry
                            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mappings.entrySet()) {
                                HandlerMethod handler = entry.getValue();
                                if (handler == null) {
                                    continue;
                                }
                                if (!handler.getMethod().equals(method)) {
                                    continue;
                                }

                                RequestMappingInfo mappingInfo = entry.getKey();
                                String fullPath = resolveFirstPattern(mappingInfo, basePath);
                                RequestMethod httpMethod = resolveFirstHttpMethod(mappingInfo);

                                actions.add(ActionInfoDto.builder()
                                        .actionId(actionInfo.id())
                                        .name(actionInfo.name())
                                        .description(actionInfo.description())
                                        .featureId(featureInfo.id())
                                        .path(fullPath)
                                        .requestMethod(httpMethod)
                                        .accessLevel((featureInfo.accessLevel().equals(AccessLevel.NONE)) ? actionInfo.accessLevel() : featureInfo.accessLevel())
                                        .securityLevel(actionInfo.securityLevel())
                                        .requestModel(requestBody)
                                        .requestMultipart(requestMultipartBodySet)
                                        .requestParams(requestParams)
                                        .responseModel(responseBody)
                                        .operationName(method.getName())
                                        .build());
                            }
                        } catch (Exception me) {
                            log.error("Error scanning method {}.{} — skipping method: {}", clazz.getName(), method.getName(), me.getMessage(), me);
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
                } catch (Exception ce) {
                    log.error("Error scanning class {} — skipping class: {}", clazz == null ? "null" : clazz.getName(), ce.getMessage(), ce);
                }
            }

            endpointDetails.setId(this.applicationConfigProperties.getApplicationName());
            endpointDetails.setName(this.applicationConfigProperties.getApplicationName());
            endpointDetails.setClientId(this.applicationConfigProperties.getXApiKey());
            endpointDetails.setClientSecret(this.applicationConfigProperties.getXApiSecret());
            endpointDetails.setModels(dtoDescriptions);
            endpointDetails.setFeatures(features);

            return ApiResponseDto.<EndpointDetailsDto>builder()
                    .success(true)
                    .opStatus(0)
                    .responseData(endpointDetails)
                    .build();

        } catch (Exception e) {
            log.error("Error scanning endpoints: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1009.getErrorResponseBody(e.getMessage()), HttpStatus.OK);
        }
    }

    /**
     * Resolve first pattern from RequestMappingInfo safely.
     * Falls back to defaultPath when no pattern is available.
     * Supports both legacy PatternsRequestCondition and newer PathPatternsRequestCondition (via reflection).
     */
    private String resolveFirstPattern(RequestMappingInfo mappingInfo, String defaultPath) {
        if (mappingInfo == null) {
            return defaultPath;
        }

        try {
            // 1) legacy approach
            PatternsRequestCondition patternsCondition = mappingInfo.getPatternsCondition();
            if (patternsCondition != null) {
                return patternsCondition.getPatterns().stream().findFirst().orElse(defaultPath);
            }

            // 2) try newer PathPatternsRequestCondition via reflection (to remain compatible across Spring versions)
            try {
                Object pathCond = mappingInfo.getClass().getMethod("getPathPatternsCondition").invoke(mappingInfo);
                if (pathCond != null) {
                    // try getPatternValues() first
                    try {
                        Object values = pathCond.getClass().getMethod("getPatternValues").invoke(pathCond);
                        if (values instanceof Collection) {
                            Collection<?> coll = (Collection<?>) values;
                            if (!coll.isEmpty()) {
                                return String.valueOf(coll.iterator().next());
                            }
                        }
                    } catch (NoSuchMethodException nsme) {
                        // fallback to getPatterns() on the pathCond object if available
                        try {
                            Object patterns = pathCond.getClass().getMethod("getPatterns").invoke(pathCond);
                            if (patterns instanceof Collection) {
                                Collection<?> coll = (Collection<?>) patterns;
                                if (!coll.isEmpty()) {
                                    return String.valueOf(coll.iterator().next());
                                }
                            }
                        } catch (NoSuchMethodException ignore) {
                            // nothing else to try
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {
                // running on older Spring where getPathPatternsCondition doesn't exist
            }
        } catch (Exception ex) {
            log.debug("Error while resolving mapping patterns: {}", ex.getMessage());
        }

        return defaultPath;
    }

    /**
     * Resolve first HTTP method from RequestMappingInfo safely. Defaults to GET when none present.
     */
    private RequestMethod resolveFirstHttpMethod(RequestMappingInfo mappingInfo) {
        if (mappingInfo == null) {
            return RequestMethod.GET;
        }
        try {
            if (mappingInfo.getMethodsCondition() != null && !mappingInfo.getMethodsCondition().getMethods().isEmpty()) {
                return mappingInfo.getMethodsCondition().getMethods().stream().findFirst().orElse(RequestMethod.GET);
            }
        } catch (Exception ex) {
            log.debug("Error while resolving request method: {}", ex.getMessage());
        }
        return RequestMethod.GET;
    }

    private BaseType resolveBaseType(Parameter parameter) {

        Class<?> type = parameter.getType();

        // --- Collection types ---
        if (Collection.class.isAssignableFrom(type)) {
            if (Set.class.isAssignableFrom(type)) {
                return BaseType.SET;
            }
            return BaseType.LIST;
        }

        if (Map.class.isAssignableFrom(type)) {
            return BaseType.MAP;
        }

        // --- Primitive & wrapper types ---
        if (type.equals(String.class)) {
            return BaseType.STRING;
        }
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return BaseType.INTEGER;
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return BaseType.LONG;
        }
        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return BaseType.BOOLEAN;
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
            return BaseType.FLOAT;
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return BaseType.DOUBLE;
        }

        // --- Date types ---
        if (Date.class.isAssignableFrom(type) ||
                type.getName().startsWith("java.time")) {
            return BaseType.DATE;
        }

        // --- Fallback ---
        if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
            return BaseType.OBJECT;
        }

        return BaseType.UNKNOWN;
    }

    private String resolveRequestParamKey(Parameter parameter) {
        RequestParam rp = parameter.getAnnotation(RequestParam.class);

        if (rp == null) {
            return parameter.getName();
        }

        if (!rp.name().isEmpty()) {
            return rp.name();
        }

        if (!rp.value().isEmpty()) {
            return rp.value();
        }

        return parameter.getName();
    }

}