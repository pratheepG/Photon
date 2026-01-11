package com.photon.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.SecurityLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionInfoDto {
    private UUID id;
    private String actionId;
    private SecurityLevel securityLevel;
    @JsonSetter(nulls = Nulls.SET, contentNulls = Nulls.SET)
    private AccessLevel accessLevel;
    private String path;
    private String name;
    private RequestMethod requestMethod;
    private String description;
    private String featureId;
    private Map<String,Map<Long, String>> userRoles = new HashMap<>();
    private FeatureInfoDto feature;
    
    private String operationName;
    private Set<ApiTypeNodeDto> requestParams = new HashSet<>();
    private Set<ApiTypeNodeDto> requestHeaders = new HashSet<>();
    private ApiTypeNodeDto responseSchema;
    private ApiTypeNodeDto requestSchema;
    private Set<ApiTypeNodeDto> multipartSchema = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionInfoDto that = (ActionInfoDto) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(name, that.name) &&
                requestMethod == that.requestMethod &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name, requestMethod, description);
    }
}