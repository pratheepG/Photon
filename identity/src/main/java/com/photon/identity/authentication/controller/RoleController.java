package com.photon.identity.authentication.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.request.RoleFeatureActionRequestDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.identity.authentication.dto.RoleDto;
import com.photon.identity.authentication.dto.response.RoleResponseDto;
import com.photon.identity.authentication.service.RoleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/role")
@FeatureInfo(id = "ROLE", name = "User Roles", description = "User Role API")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ActionInfo(id = "CREATE_ROLE", name = "CREATE_ROLE", accessLevel = AccessLevel.ADMIN, description = "Create the roles from back-office/admin-panel")
    public ResponseEntity<ApiResponseDto<?>> addRole(@RequestBody RoleDto role) {
        return ResponseEntity.ok().body(this.roleService.createNewRole(role));
    }

    @PutMapping
    @ActionInfo(id = "UPDATE_ROLE", name = "UPDATE_ROLE", accessLevel = AccessLevel.ADMIN, description = "Update the role details from back-office/admin-panel")
    public ResponseEntity<ApiResponseDto<?>> updateRole(@RequestBody RoleDto role) {
        return ResponseEntity.ok().body(this.roleService.updateRole(role));
    }

    @GetMapping
    @ActionInfo(id = "GET_ALL_ROLE_BY_PAGE", name = "GET_ALL_ROLE_BY_PAGE", accessLevel = AccessLevel.VIEWER, description = "Get all roles by page")
    public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getAllRoles(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.roleService.getAllRoles(pageNumber, pageSize));
    }

    @GetMapping(value = "/lookup")
    @ActionInfo(id = "GET_ALL_ROLE_LOOKUP_BY_PAGE", name = "GET_ALL_ROLE_LOOKUP_BY_PAGE", accessLevel = AccessLevel.VIEWER, description = "Get all role lookup by page")
    public ResponseEntity<ApiResponseDto<?>> getRoles(@RequestParam(required = false, defaultValue = "VIEWER") AccessLevel accessLevel, @RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.roleService.getRoles(accessLevel, pageNumber, pageSize));
    }

    @GetMapping(value = "/{id}")
    @ActionInfo(id = "GET_ROLE_BY_ID", name = "GET_ROLE_BY_ID", accessLevel = AccessLevel.VIEWER, description = "Get role by id")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(this.roleService.findRoleById(id));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteRoleById(@PathVariable("id") Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        return ResponseEntity.ok().body(this.roleService.deleteRolesByIds(ids));
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> updateRole(@RequestBody RoleDto role, @PathVariable("id") Long id) {
        return ResponseEntity.ok().body(this.roleService.patchRole(id, role));
    }

    @PatchMapping(value = "/feature-action/{id}")
    public ResponseEntity<ApiResponseDto<?>> addFeatureActionInRole(@RequestBody Map<String, Set<String>> featureActions, @PathVariable("id") Long id) {
        return ResponseEntity.ok().body(this.roleService.addFeatureActionInRole(id, featureActions));
    }

    @DeleteMapping(path = "/feature-action/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteFeatureActionFromRole(@RequestBody Map<String, Set<String>> featureActions, @PathVariable("id") Long id) {
        return ResponseEntity.ok().body(this.roleService.deleteFeatureActionFromRole(id, featureActions));
    }

    @PatchMapping(value = "/feature-action")
    public ResponseEntity<ApiResponseDto<?>> addAllFeatureActionInRole(@RequestBody List<RoleFeatureActionRequestDto> roleFeatureActionRequestList) {
        return ResponseEntity.ok().body(this.roleService.addAllFeatureActionInRole(roleFeatureActionRequestList));
    }

    @DeleteMapping(value = "/feature-action")
    public ResponseEntity<ApiResponseDto<?>> deleteAllFeatureActionFromRole(@RequestBody List<RoleFeatureActionRequestDto> roleFeatureActionRequestList) {
        return ResponseEntity.ok().body(this.roleService.deleteAllFeatureActionInRole(roleFeatureActionRequestList));
    }
}