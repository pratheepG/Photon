package com.photon.console.entity;

import com.photon.console.entity.converter.MapToJsonConverter;
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

//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "f_id")
//    private FeatureInfo feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "f_id", nullable = false)
    private FeatureInfo feature;


    @Column(name = "feature_id", nullable = false)
    private String featureId;

    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "request_body_model_id")
    private String requestBodyModelId;

    @Column(name = "is_request_body_collection")
    private Boolean isRequestBodyCollection;

    @Column(name = "response_body_model_id")
    private String responseBodyModelId;

    @Column(name = "is_response_body_collection")
    private Boolean isResponseBodyCollection;

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