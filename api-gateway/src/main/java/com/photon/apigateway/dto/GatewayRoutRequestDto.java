package com.photon.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.apigateway.entity.GatewayRoute;
import com.photon.auth.enums.AuthFilter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.net.URI;
import java.util.*;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRoutRequestDto {

    private String routeId;
    private String path;
    private String method;
    private String applicationId;
    private Set<String> roles;
    private Set<String> idps;
    private AuthFilter authFilter;
    private long maxSizeBytes = -1;
    private long replenishRate = -1;
    private long burstCapacity = -1;
    private RouteDefinition routeDefinition;

    @JsonCreator
    public GatewayRoutRequestDto(
            @JsonProperty("routeId") String routeId,
            @JsonProperty("path") String path,
            @JsonProperty("method") String method,
            @JsonProperty("applicationId") String applicationId,
            @JsonProperty("roles") Set<String> roles,
            @JsonProperty("idps") Set<String> idps,
            @JsonProperty("authFilter") AuthFilter authFilter
    ) {
        this.routeId = routeId;
        this.path = path;
        this.method = method;
        this.applicationId = applicationId;
        this.roles = roles;
        this.idps = idps;
        this.authFilter = authFilter;
        this.postConstruct();
    }

    void postConstruct(){
        PredicateDefinition pathPredicates = new PredicateDefinition();
        pathPredicates.addArg("_genkey_0", this.path);
        pathPredicates.setName("Path");

        PredicateDefinition methodPredicates = new PredicateDefinition();
        methodPredicates.addArg("_genkey_0", this.method);
        methodPredicates.setName("Method");

        List<FilterDefinition> filters = new ArrayList<>();
        FilterDefinition filterDefinition = new FilterDefinition();

        if(this.authFilter.equals(AuthFilter.AUTHENTICATED)) {
            filterDefinition.setName(this.authFilter.getBean());
            filterDefinition.addArg("roles", String.join(",", this.roles));
            filterDefinition.addArg("idps", String.join(",", this.idps));
            filters.add(filterDefinition);
        } else if (this.authFilter.equals(AuthFilter.ANONYMOUS)) {
            filterDefinition.setName(this.authFilter.getBean());
            filters.add(filterDefinition);
        } else if (this.authFilter.equals(AuthFilter.PROSPECT)) {
            filterDefinition.setName(this.authFilter.getBean());
            filters.add(filterDefinition);
        }

        if(this.maxSizeBytes>-1) {
            FilterDefinition dataTransferSizeFilterDef = new FilterDefinition();
            dataTransferSizeFilterDef.setName("RequestSize");
            dataTransferSizeFilterDef.addArg("maxSize", String.valueOf(maxSizeBytes));
            filters.add(dataTransferSizeFilterDef);
        }

        if(replenishRate>-1 && burstCapacity>-1) {
            FilterDefinition rateFilter = new FilterDefinition();
            rateFilter.setName("RequestRateLimiter");
            Map<String, String> args = new HashMap<>();
            args.put("key-resolver", "#{@ipKeyResolver}");
            args.put("redis-rate-limiter.replenishRate", String.valueOf(replenishRate));
            args.put("redis-rate-limiter.burstCapacity", String.valueOf(burstCapacity));
            rateFilter.setArgs(args);
            filters.add(rateFilter);
        }

        this.routeDefinition = new RouteDefinition();
        this.routeDefinition.setId(this.routeId);
        this.routeDefinition.setUri(URI.create("lb://" + applicationId));
        this.routeDefinition.setPredicates(Arrays.asList(pathPredicates,methodPredicates));
        this.routeDefinition.setOrder(0);

        if (this.authFilter.equals(AuthFilter.AUTHENTICATED) || this.authFilter.equals(AuthFilter.ANONYMOUS))
            this.routeDefinition.setFilters(filters);
    }

    public GatewayRoute toEntity() {
        try {
            this.postConstruct();
            GatewayRoute gatewayRoute = new GatewayRoute();
            ObjectMapper mapper = new ObjectMapper();
            String routeJson = mapper.writeValueAsString(this.routeDefinition);
            gatewayRoute.setRouteId(routeId);
            gatewayRoute.setRouteDefinition(routeJson);
            return gatewayRoute;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

}