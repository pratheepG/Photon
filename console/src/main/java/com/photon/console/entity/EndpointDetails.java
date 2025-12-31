package com.photon.console.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "endpoint_details")
public class EndpointDetails {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

//    @ToString.Exclude
//    @OneToMany(mappedBy = "endpointDetails", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<FeatureInfo> features = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "endpointDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeatureInfo> features;

    @OneToMany(mappedBy = "endpointDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Model> models = new LinkedHashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointDetails that = (EndpointDetails) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientSecret, that.clientSecret) &&
                Objects.equals(name, that.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(clientId, clientSecret, name);
    }
}