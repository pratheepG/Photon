package com.photon.console.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal mirror of Gateway's RouteDefinition JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouteDefinition {
    private String id;
    private String uri;
    private Integer order = 0;
    private List<PredicateDefinition> predicates = new ArrayList<>();
    private List<FilterDefinition> filters = new ArrayList<>();
    private Map<String, Object> metadata = new LinkedHashMap<>();

    @JsonProperty("id")        public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @JsonProperty("uri")       public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    @JsonProperty("order")     public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    @JsonProperty("predicates") public List<PredicateDefinition> getPredicates() { return predicates; }
    public void setPredicates(List<PredicateDefinition> predicates) { this.predicates = predicates; }

    @JsonProperty("filters")   public List<FilterDefinition> getFilters() { return filters; }
    public void setFilters(List<FilterDefinition> filters) { this.filters = filters; }

    @JsonProperty("metadata")  public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}