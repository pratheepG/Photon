package com.photon.console.gateway.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.auth.enums.AuthFilter;
import com.photon.console.gateway.entity.GatewayRoute;

import java.util.*;

/**
 * Pure-Java builder that produces the same JSON Spring Cloud Gateway expects,
 * without importing gateway classes.
 */
public class GatewayRoutRequestDto {

    private String routeId;
    private String path;
    private String method;
    private String applicationId;
    private Set<String> roles;
    private Set<String> idps;

    private AuthFilter authFilter;
    private String filterName;

    private RouteDefinition routeDefinition;

    public GatewayRoutRequestDto() {}

    @JsonCreator
    public GatewayRoutRequestDto(
            @JsonProperty("routeId") String routeId,
            @JsonProperty("path") String path,
            @JsonProperty("method") String method,
            @JsonProperty("applicationId") String applicationId,
            @JsonProperty("roles") Set<String> roles,
            @JsonProperty("idps") Set<String> idps,
            @JsonProperty("authFilter") AuthFilter authFilter,
            @JsonProperty("filterName") String filterName
    ) {
        this.routeId = routeId;
        this.path = path;
        this.method = method;
        this.applicationId = applicationId;
        this.roles = roles != null ? roles : Collections.emptySet();
        this.idps = idps != null ? idps : Collections.emptySet();
        this.authFilter = authFilter;
        this.filterName = filterName;
        buildRouteDefinition();
    }

    /** Builds SimpleRouteDefinition matching your stored shape exactly. */
    private void buildRouteDefinition() {
        PredicateDefinition pathPredicate = new PredicateDefinition("Path");
        pathPredicate.getArgs().put("_genkey_0", nullSafe(this.path));

        PredicateDefinition methodPredicate = new PredicateDefinition("Method");
        methodPredicate.getArgs().put("_genkey_0", nullSafe(this.method));

        List<FilterDefinition> filters = new ArrayList<>();

        if (filterName != null && !filterName.isBlank()) {
            filters.add(new FilterDefinition(filterName));
        } else if (authFilter != null) {
            if (authFilter == AuthFilter.AUTHENTICATED) {
                FilterDefinition f = new FilterDefinition(authFilter.getBean());
                if (!roles.isEmpty()) f.getArgs().put("roles", String.join(",", roles));
                if (!idps.isEmpty())  f.getArgs().put("idps", String.join(",", idps));
                filters.add(f);
            } else if (authFilter == AuthFilter.ANONYMOUS) {
                filters.add(new FilterDefinition(authFilter.getBean()));
            }
        }

        RouteDefinition rd = new RouteDefinition();
        rd.setId(nullSafe(this.routeId));
        rd.setUri("lb://" + nullSafe(this.applicationId));
        rd.setOrder(0);
        rd.setMetadata(new LinkedHashMap<>());
        rd.setPredicates(Arrays.asList(pathPredicate, methodPredicate));
        rd.setFilters(filters);

        this.routeDefinition = rd;
    }

    private static String nullSafe(String v) { return v == null ? "" : v; }

    /** Produces the JSON string you’ll persist (same shape as Gateway). */
    public String toRouteJson() {
        try {
            buildRouteDefinition();
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writeValueAsString(this.routeDefinition);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize route", e);
        }
    }

    /** Convenience like your original: wraps JSON into a persistence entity. */
    public GatewayRoute toEntity() {
        GatewayRoute entity = new GatewayRoute();
        entity.setRouteId(this.routeId);
        entity.setRouteDefinition(toRouteJson());
        return entity;
    }

    // -------- Getters/Setters --------
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Set<String> getIdps() { return idps; }
    public void setIdps(Set<String> idps) { this.idps = idps; }

    public AuthFilter getAuthFilter() { return authFilter; }
    public void setAuthFilter(AuthFilter authFilter) { this.authFilter = authFilter; }

    public String getFilterName() { return filterName; }
    public void setFilterName(String filterName) { this.filterName = filterName; }

    public RouteDefinition getRouteDefinition() { return routeDefinition; }
    public void setRouteDefinition(RouteDefinition routeDefinition) { this.routeDefinition = routeDefinition; }
}