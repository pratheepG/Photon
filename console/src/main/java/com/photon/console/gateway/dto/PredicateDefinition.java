package com.photon.console.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/** Mirror of Gateway PredicateDefinition (name + args). */
public class PredicateDefinition {
    private String name;
    private Map<String, String> args = new LinkedHashMap<>();

    public PredicateDefinition() {}
    public PredicateDefinition(String name) { this.name = name; }

    @JsonProperty("name") public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("args") public Map<String, String> getArgs() { return args; }
    public void setArgs(Map<String, String> args) { this.args = args; }
}