package com.photon.console.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/** Mirror of Gateway FilterDefinition (name + args). */
public class FilterDefinition {
    private String name;
    private Map<String, String> args = new LinkedHashMap<>();

    public FilterDefinition() {}
    public FilterDefinition(String name) { this.name = name; }

    @JsonProperty("name") public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("args") public Map<String, String> getArgs() { return args; }
    public void setArgs(Map<String, String> args) { this.args = args; }
}