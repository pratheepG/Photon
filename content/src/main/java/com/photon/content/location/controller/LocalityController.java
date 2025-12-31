package com.photon.content.location.controller;

import com.photon.content.location.dto.response.DistrictResponse;
import com.photon.content.location.dto.response.StateResponse;
import com.photon.content.location.dto.response.CityResponse;
import com.photon.content.location.service.LocalityService;
import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.enums.AccessLevel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locality")
@FeatureInfo(id = "LOCALITY", name = "Localities API", description = "Localities API for location/state/district")
public class LocalityController {

    private final LocalityService service;

    public LocalityController(LocalityService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/localities/states?q=:q&page=:page&size=:size
     * Search states by name (partial, case-insensitive). If q absent, returns paged list of all states.
     */
    @GetMapping(value = "/state", produces = MediaType.APPLICATION_JSON_VALUE)
    @ActionInfo(id = "SEARCH_STATE", accessLevel = AccessLevel.VIEWER, name = "SEARCH_STATES", description = "Search states by query")
    public Mono<ApiResponseDto<List<StateResponse>>> searchStates(
            @RequestParam(value = "query", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        return service.searchStates(q, page, size);
    }

    /**
     * GET /api/v1/localities/states/{stateCode}
     * Get one state by state code (e.g. OR)
     */
    @GetMapping(value = "/state/{stateCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ActionInfo(id = "GET_STATE_BY_CODE", accessLevel = AccessLevel.VIEWER, name = "GET_STATE_BY_CODE", description = "Get state by code")
    public Mono<ApiResponseDto<StateResponse>> getStateByCode(@PathVariable("stateCode") String stateCode) {
        return service.getStateByCode(stateCode);
    }

    /**
     * GET /api/v1/localities/districts?stateId=:stateId&q=:q&page=:page&size=:size
     * Search districts optionally scoped to a state code.
     */
    @GetMapping(value = "/district", produces = MediaType.APPLICATION_JSON_VALUE)
    @ActionInfo(id = "SEARCH_DISTRICT", accessLevel = AccessLevel.VIEWER, name = "SEARCH_DISTRICT", description = "Search district by query")
    public Mono<ApiResponseDto<List<DistrictResponse>>> searchDistricts(
            @RequestParam(value = "query", required = false) String q,
            @RequestParam(value = "stateId", required = false) Long stateId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        return service.searchDistricts(q, stateId, page, size);
    }

    /**
     * GET /api/v1/locality/city?districtId=:districtId&q=:q&pincode=:pincode&page=:page&size=:size
     * Search city with optional district id and/or pincode.
     */
    @GetMapping(value = "/city", produces = MediaType.APPLICATION_JSON_VALUE)
    @ActionInfo(id = "SEARCH_CITY", accessLevel = AccessLevel.VIEWER, name = "SEARCH_CITY", description = "Search city by query")
    public Mono<ApiResponseDto<List<CityResponse>>> searchCity(
            @RequestParam(value = "query", required = false) String q,
            @RequestParam(value = "districtId", required = false) Long districtId,
            @RequestParam(value = "pinCode", required = false) String pinCode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        return service.searchCity(q, districtId, pinCode, page, size);
    }
}