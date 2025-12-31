package com.photon.identity.authentication.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.dto.request.ServerConsoleUserDto;
import com.photon.identity.authentication.dto.request.ServerConsoleFirstUserDto;
import com.photon.identity.authentication.dto.request.UserByAdminRequestDto;
import com.photon.identity.authentication.dto.response.UserListDTO;
import com.photon.identity.authentication.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/user")
@FeatureInfo(id = "USER", name = "User records", description = "Users API")
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/create-user")
    @ActionInfo(id = "CREATE_USER", name = "CREATE_USER", accessLevel = AccessLevel.ADMIN, description = "Create the user from back-office/admin-panel")
    public ResponseEntity<ApiResponseDto<?>> saveUser(@RequestBody UserByAdminRequestDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @GetMapping(value = "/get-user-details")
    @ActionInfo(id = "GET_USER_DETAILS", name = "GET_USER_DETAILS", accessLevel = AccessLevel.OWNER, description = "Get the users details")
    public ResponseEntity<ApiResponseDto<UserDto>> getUserDetails(@RequestParam(required = false) String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetails(userId));
    }

    @GetMapping(value = "/get-user")
    @ActionInfo(id = "GET_ALL_USER_BY_PAGE", name = "GET_ALL_USER_BY_PAGE", accessLevel = AccessLevel.ADMIN, description = "Get the users list for back-office/admin-panel")
    public ResponseEntity<ApiResponseDto<List<UserDto>>> getAllUser(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUser(pageNumber, pageSize));
    }

    @GetMapping(value = "/get-user-list")
    @ActionInfo(id = "GET_USER_LIST_BY_PAGE", name = "GET_USER_LIST_BY_PAGE", accessLevel = AccessLevel.ADMIN, description = "Get the users list with min data for back-office/admin-panel")
    public ResponseEntity<ApiResponseDto<List<UserListDTO>>> getUserList(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUserMinData(pageNumber, pageSize));
    }

    @PatchMapping(value = "/update-user-status")
    @ActionInfo(id = "UPDATE_USER_STATUS", name = "UPDATE_USER_STATUS", accessLevel = AccessLevel.ADMIN, description = "Update user status")
    public ResponseEntity<ApiResponseDto<?>> updateUserStatus(@RequestParam boolean isEnabled, String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserStatus(isEnabled, userId));
    }

    @PatchMapping(value = "/update-user-by-admin")
    @ActionInfo(id = "UPDATE_USER_BY_ADMIN", name = "UPDATE_USER_BY_ADMIN", accessLevel = AccessLevel.ADMIN, description = "Update user by admin")
    public ResponseEntity<ApiResponseDto<?>> updateUserAdmin(@RequestBody UserByAdminRequestDto user, @RequestParam String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserByAdmin(user, userId));
    }

    @PostMapping(value = "/{userId}/add-user-tenant")
    @ActionInfo(id = "ADD_USER_TENANT_BY_ADMIN", name = "ADD_USER_TENANT_BY_ADMIN", accessLevel = AccessLevel.ADMIN, description = "Add user-tenant by admin")
    public ResponseEntity<ApiResponseDto<?>> addUserTenant(@RequestBody List<UUID> tenants, @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addUserTenant(tenants, userId));
    }

    @PostMapping(value = "/{userId}/remove-user-tenant")
    @ActionInfo(id = "REMOVE_USER_TENANT_BY_ADMIN", name = "REMOVE_USER_TENANT_BY_ADMIN", accessLevel = AccessLevel.ADMIN, description = "Remove user-tenant by admin")
    public ResponseEntity<ApiResponseDto<?>> removeUserTenant(@RequestBody List<UUID> tenants, @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.removeUserTenant(tenants, userId));
    }

    @PostMapping(value = "/register-first-user")
    public ResponseEntity<ApiResponseDto<?>> saveFirstUser(@RequestBody ServerConsoleFirstUserDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveFirstUser(user));
    }

    @PostMapping(value = "/register-server-user")
    public ResponseEntity<ApiResponseDto<?>> saveServerUser(@RequestBody ServerConsoleUserDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveServerConsoleUser(user));
    }

    @GetMapping(value = "/get-server-user")
    public ResponseEntity<ApiResponseDto<?>> getAllServerUser(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getAllServerConsoleUser(pageNumber, pageSize));
    }

}