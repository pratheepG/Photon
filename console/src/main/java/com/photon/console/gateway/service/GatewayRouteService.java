package com.photon.console.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.console.gateway.dto.GatewayRoutRequestDto;
import com.photon.console.gateway.dto.RouteDefinition;
import com.photon.console.gateway.entity.GatewayRoute;
import com.photon.console.gateway.repository.GatewayRouteRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.SuccessEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class GatewayRouteService {

    private final GatewayRouteRepository gatewayRouteRepository;
    private final ObjectMapper objectMapper;

    public GatewayRouteService(GatewayRouteRepository gatewayRouteRepository, ObjectMapper objectMapper) {
        this.gatewayRouteRepository = gatewayRouteRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApiResponseDto<?> addOrUpdatePath(String routeId, GatewayRoutRequestDto gatewayRoutRequestDto) {
        log.debug("addPath routeId: {}, path: {}", routeId, gatewayRoutRequestDto.getPath());
        try {
            Optional<RouteDefinition> routeDefinitionOpt = getRouteById(routeId);
            if (routeDefinitionOpt.isPresent()) {
                log.debug("Route found: {}", routeId);
                GatewayRoute gatewayRoute = gatewayRoutRequestDto.toEntity();
                gatewayRouteRepository.deleteByRouteId(routeId);
                gatewayRouteRepository.save(gatewayRoute);
                return SuccessEnum.UPDATED.getSuccessResponseBody();
            } else {
                log.debug("Route not found, creating new route with ID: {}", routeId);
                GatewayRoute gatewayRoute = gatewayRoutRequestDto.toEntity();
                gatewayRouteRepository.save(gatewayRoute);
                return SuccessEnum.CREATED.getSuccessResponseBody();
            }
        } catch (Exception e) {
            log.error("Error while adding route: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add path to route", e);
        }
    }

    @Transactional
    public ApiResponseDto<?> deletePath(String routeId) {
        try {
            gatewayRouteRepository.deleteByRouteId(routeId);
            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete route with ID: " + routeId, e);
        }
    }

    private Optional<RouteDefinition> getRouteById(String id) {
        try {
            return gatewayRouteRepository.findByRouteId(id)
                    .map(gatewayRoute -> {
                        try {
                            return objectMapper.readValue(gatewayRoute.getRouteDefinition(), RouteDefinition.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize route definition", e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch route", e);
        }
    }

}