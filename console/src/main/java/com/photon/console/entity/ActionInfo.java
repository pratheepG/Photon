package com.photon.console.entity;

import com.photon.console.entity.converter.ActionModelConverter;
import com.photon.console.entity.converter.ActionMultipartConverter;
import com.photon.console.entity.converter.ActionParamConverter;
import com.photon.console.entity.converter.MapToJsonConverter;
import com.photon.endpoint.dto.ActionModelDto;
import com.photon.endpoint.dto.ActionMultipartDto;
import com.photon.endpoint.dto.ActionParamDto;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.SecurityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action_info")
public class ActionInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "action_id")
    private String actionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_level")
    private SecurityLevel securityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    private AccessLevel accessLevel;

    @Column(name = "path")
    private String path;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_method")
    private RequestMethod requestMethod;

    @Column(name = "description")
    private String description;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "user_roles", columnDefinition = "TEXT")
    private Map<String,Map<Long, String>> userRoles = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "f_id", nullable = false)
    private FeatureInfo feature;

    @Column(name = "feature_id", nullable = false)
    private String featureId;

    @Column(name = "operation_name")
    private String operationName;

    @Convert(converter = ActionParamConverter.class)
    @Column(name = "request_params", columnDefinition = "TEXT")
    private Set<ActionParamDto> requestParams = new HashSet<>();

    @Convert(converter = ActionModelConverter.class)
    @Column(name = "response_model", columnDefinition = "TEXT")
    private ActionModelDto responseModel;

    @Convert(converter = ActionModelConverter.class)
    @Column(name = "request_model", columnDefinition = "TEXT")
    private ActionModelDto requestModel;

    @Convert(converter = ActionMultipartConverter.class)
    @Column(name = "request_multipart", columnDefinition = "TEXT")
    private Set<ActionMultipartDto> requestMultipart = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionInfo that = (ActionInfo) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(name, that.name) &&
                requestMethod == that.requestMethod &&
                Objects.equals(description, that.description);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(path, name, requestMethod, description);
    }

}