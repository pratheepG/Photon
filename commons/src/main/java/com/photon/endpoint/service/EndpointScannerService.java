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
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class EndpointScannerService {

    private final ApplicationConfigProperties config;
    private final RequestMappingHandlerMapping handlerMapping;
    private final ApplicationContext applicationContext;

    public EndpointScannerService(ApplicationConfigProperties config,
                                  RequestMappingHandlerMapping requestMappingHandlerMapping,
                                  ApplicationContext applicationContext) {
        this.config = config;
        this.handlerMapping = requestMappingHandlerMapping;
        this.applicationContext = applicationContext;
    }

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    public ApiResponseDto<EndpointDetailsDto> scanEndpoints() {
        try {
            Map<RequestMappingInfo, HandlerMethod> mappings = handlerMapping.getHandlerMethods();
            String[] beanNames = applicationContext.getBeanNamesForAnnotation(FeatureInfo.class);

            Set<Class<?>> controllers = Arrays.stream(beanNames)
                    .map(applicationContext::getType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            EndpointDetailsDto endpointDetails = new EndpointDetailsDto();
            Set<FeatureInfoDto> features = new LinkedHashSet<>();
            Set<Class<?>> seenDtoClasses = new HashSet<>();
            Set<ModelDescriptionDto> dtoDescriptions = new LinkedHashSet<>();

            for (Class<?> controller : controllers) {

                FeatureInfo featureInfo = controller.getAnnotation(FeatureInfo.class);
                if (featureInfo == null) continue;

                String basePath = resolveBasePath(controller);
                Set<ActionInfoDto> actions = new LinkedHashSet<>();

                for (Method method : controller.getDeclaredMethods()) {

                    if (!method.isAnnotationPresent(ActionInfo.class)) continue;
                    ActionInfo actionInfo = method.getAnnotation(ActionInfo.class);

                    ApiTypeNodeDto requestSchema = null;
                    ApiTypeNodeDto responseSchema = null;
                    Set<ApiTypeNodeDto> multipartSchema = new LinkedHashSet<>();
                    Set<ApiTypeNodeDto> requestHeaders = new LinkedHashSet<>();
                    Set<ApiTypeNodeDto> requestParams = new LinkedHashSet<>();

                    // ---------- Parameters ----------
                    for (Parameter p : method.getParameters()) {

                        if (p.isAnnotationPresent(RequestBody.class)) {
                            requestSchema = buildApiTypeNode(p.getParameterizedType(), p.getName(), true, seenDtoClasses, dtoDescriptions);
                        }

                        if (p.isAnnotationPresent(RequestPart.class)) {
                            multipartSchema.add(buildApiTypeNode(p.getParameterizedType(), p.getName(), true, seenDtoClasses, dtoDescriptions));
                        }

                        if (p.isAnnotationPresent(RequestHeader.class)) {
                            RequestHeader rh = p.getAnnotation(RequestHeader.class);
                            requestHeaders.add(buildApiTypeNode(p.getParameterizedType(), resolveHeaderKey(p), rh.required(), seenDtoClasses, dtoDescriptions));
                        }

                        if (p.isAnnotationPresent(RequestParam.class)) {
                            RequestParam rp = p.getAnnotation(RequestParam.class);
                            requestParams.add(buildApiTypeNode(p.getParameterizedType(), resolveRequestParamKey(p), rp.required(), seenDtoClasses, dtoDescriptions));
                        }
                    }

                    // ---------- Response ----------
                    ResponseModelTypeResolver.Result resResult = ResponseModelTypeResolver.resolve(method.getGenericReturnType());

                    if (resResult.getModelClass() != null) {
                        //Type resolvedResponseType = unwrapResponseWrappers(method.getGenericReturnType());
                        responseSchema = buildResponseSchema(method.getGenericReturnType(), seenDtoClasses, dtoDescriptions);
                    }

                    // ---------- Mapping ----------
                    for (Map.Entry<RequestMappingInfo, HandlerMethod> e : mappings.entrySet()) {
                        if (!e.getValue().getMethod().equals(method)) continue;

                        RequestMappingInfo info = e.getKey();

                        actions.add(ActionInfoDto.builder()
                                .actionId(actionInfo.id())
                                .name(actionInfo.name())
                                .description(actionInfo.description())
                                .featureId(featureInfo.id())
                                .path(resolveFirstPattern(info, basePath))
                                .requestMethod(resolveFirstHttpMethod(info))
                                .accessLevel(featureInfo.accessLevel() == AccessLevel.NONE ? actionInfo.accessLevel() : featureInfo.accessLevel())
                                .securityLevel(actionInfo.securityLevel())
                                .requestSchema(requestSchema)
                                .multipartSchema(multipartSchema)
                                .responseSchema(responseSchema)
                                .requestHeaders(requestHeaders)
                                .requestParams(requestParams)
                                .operationName(method.getName())
                                .build());
                    }
                }

                features.add(FeatureInfoDto.builder()
                        .featureId(featureInfo.id())
                        .name(featureInfo.name())
                        .moduleName(controller.getSimpleName())
                        .path(basePath)
                        .description(featureInfo.description())
                        .actions(actions)
                        .build());
            }

            endpointDetails.setId(config.getApplicationName());
            endpointDetails.setName(config.getApplicationName());
            endpointDetails.setClientId(config.getXApiKey());
            endpointDetails.setClientSecret(config.getXApiSecret());
            endpointDetails.setModels(dtoDescriptions);
            endpointDetails.setFeatures(features);

            return ApiResponseDto.<EndpointDetailsDto>builder()
                    .success(true)
                    .opStatus(0)
                    .responseData(endpointDetails)
                    .build();

        } catch (Exception e) {
            log.error("Endpoint scan failed", e);
            throw new ApplicationException(
                    ExceptionEnum.ERR_1009.getErrorResponseBody(e.getMessage()),
                    HttpStatus.OK
            );
        }
    }

    // ==========================================================
    // TYPE TREE BUILDER (CORE FIX)
    // ==========================================================

    private ApiTypeNodeDto buildApiTypeNode(Type type, String key, boolean required, Set<Class<?>> seenDtoClasses, Set<ModelDescriptionDto> dtoDescriptions) {

        ApiTypeNodeDto.ApiTypeNodeDtoBuilder b = ApiTypeNodeDto.builder().key(key).required(required);

        // ---------- Parameterized ----------
        if (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();

            if (raw instanceof Class<?> rawClass) {

                // LIST / SET
                if (Collection.class.isAssignableFrom(rawClass)) {
                    b.type(Set.class.isAssignableFrom(rawClass) ? BaseType.SET : BaseType.LIST);
                    b.element(buildApiTypeNode(pt.getActualTypeArguments()[0], null, true, seenDtoClasses, dtoDescriptions));
                    return b.build();
                }

                // MAP
                if (Map.class.isAssignableFrom(rawClass)) {
                    b.type(BaseType.MAP);
                    b.map(MapNodeDto.builder()
                            .key(buildApiTypeNode(pt.getActualTypeArguments()[0], null, true, seenDtoClasses, dtoDescriptions))
                            .value(buildApiTypeNode(pt.getActualTypeArguments()[1], null, true, seenDtoClasses, dtoDescriptions))
                            .build());
                    return b.build();
                }
            }
        }

        // ---------- Raw class ----------
        if (type instanceof Class<?> clazz) {

            BaseType base = resolveBaseType(clazz);
            if (base != BaseType.OBJECT) {
                b.type(base);
                return b.build();
            }

            b.type(BaseType.DTO);
            b.modelId(clazz.getCanonicalName());

            if (seenDtoClasses.add(clazz)) {
                dtoDescriptions.addAll(ModelIntrospector.buildDtoGraph(clazz));
            }
            return b.build();
        }

        b.type(BaseType.UNKNOWN);
        return b.build();
    }


    private Type unwrapTransport(Type type) {

        while (type instanceof ParameterizedType pt) {
            Type raw = pt.getRawType();

            if (!(raw instanceof Class<?> rawClass)) break;

            // Reactive wrappers
            if (rawClass.getName().equals("reactor.core.publisher.Mono") ||
                    rawClass.getName().equals("reactor.core.publisher.Flux")) {
                type = pt.getActualTypeArguments()[0];
                continue;
            }

            // HTTP wrapper
            if (rawClass == org.springframework.http.ResponseEntity.class) {
                type = pt.getActualTypeArguments()[0];
                continue;
            }

            break;
        }
        return type;
    }

    private ApiTypeNodeDto buildResponseSchema(Type returnType, Set<Class<?>> seenDtoClasses, Set<ModelDescriptionDto> dtoDescriptions) {

        /* 1️⃣ unwrap Mono / Flux / ResponseEntity */
        Type type = unwrapTransport(returnType);

        /* 2️⃣ ApiResponseDto<T> */
        if (type instanceof ParameterizedType pt &&
                pt.getRawType() instanceof Class<?> raw &&
                raw == com.photon.dto.ApiResponseDto.class) {

            ApiTypeNodeDto.ApiTypeNodeDtoBuilder parent = ApiTypeNodeDto.builder()
                    .key("-")
                    .type(BaseType.DTO)
                    .modelId(raw.getCanonicalName())
                    .required(true);

            // 🔹 Defensive: check inner payload existence
            Type[] args = pt.getActualTypeArguments();
            if (args != null && args.length == 1) {

                Type inner = args[0];

                // 🔹 Ignore meaningless payloads
                if (inner != null && inner != Void.class && inner != void.class
                        && inner != Object.class && !(inner instanceof WildcardType)) {

                    ApiTypeNodeDto payload = buildApiTypeNode(inner, null, true, seenDtoClasses, dtoDescriptions);

                    if (payload != null && payload.getType() != BaseType.UNKNOWN) {
                        parent.element(payload);
                    }
                }
            }

            return parent.build();
        }

        /* 3️⃣ Normal response (no ApiResponseDto) */
        return buildApiTypeNode(type, "-", true, seenDtoClasses, dtoDescriptions);
    }


    // ==========================================================
    // HELPERS
    // ==========================================================
    private String resolveBasePath(Class<?> clazz) {
        RequestMapping rm = clazz.getAnnotation(RequestMapping.class);
        return (rm != null && rm.value().length > 0) ? rm.value()[0] : "";
    }

    private String resolveRequestParamKey(Parameter p) {
        RequestParam rp = p.getAnnotation(RequestParam.class);
        if (!rp.name().isEmpty()) return rp.name();
        if (!rp.value().isEmpty()) return rp.value();
        return p.getName();
    }

    private String resolveHeaderKey(Parameter p) {
        RequestHeader rh = p.getAnnotation(RequestHeader.class);
        if (!rh.name().isEmpty()) return rh.name();
        if (!rh.value().isEmpty()) return rh.value();
        return p.getName();
    }

    private String resolveFirstPattern(RequestMappingInfo info, String fallback) {
        PatternsRequestCondition pc = info.getPatternsCondition();
        if (pc != null && !pc.getPatterns().isEmpty()) {
            return pc.getPatterns().iterator().next();
        }
        return fallback;
    }

    private RequestMethod resolveFirstHttpMethod(RequestMappingInfo info) {
        if (!info.getMethodsCondition().getMethods().isEmpty()) {
            return info.getMethodsCondition().getMethods().iterator().next();
        }
        return RequestMethod.GET;
    }

    private BaseType resolveBaseType(Class<?> type) {
        if (type == String.class) return BaseType.STRING;
        if (type == Integer.class || type == int.class) return BaseType.INTEGER;
        if (type == Long.class || type == long.class) return BaseType.LONG;
        if (type == Boolean.class || type == boolean.class) return BaseType.BOOLEAN;
        if (type == Float.class || type == float.class) return BaseType.FLOAT;
        if (type == Double.class || type == double.class) return BaseType.DOUBLE;
        if (Date.class.isAssignableFrom(type) || type.getName().startsWith("java.time")) return BaseType.DATE;
        return BaseType.OBJECT;
    }
}