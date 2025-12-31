package com.photon.identity.authentication.controller;

import com.photon.constants.ApplicationConstants;
import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.identity.authentication.dto.request.AbstractAuthRequest;
import com.photon.identity.authentication.dto.request.ActivateUserAuthenticationDto;
import com.photon.identity.authentication.dto.request.UserRequestDto;
import com.photon.identity.authentication.dto.response.UserDeviceRegistrationResponseDto;
import com.photon.identity.authentication.service.IdentityService;
import com.photon.identity.authentication.service.UserDeviceService;
import com.photon.identity.authentication.service.UserService;
import com.photon.identity.commons.enums.IdentityProviderOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author pratheepg
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/authentication")
@FeatureInfo(id = "AUTHENTICATION", name = "Authentication", description = "User authentication APIs")
public class AuthenticationController {

	private final IdentityService identityService;
	private final UserDeviceService userDeviceService;
	private final UserService userService;

	@PostMapping(value = "/init")
	public ResponseEntity<ApiResponseDto<UserDeviceRegistrationResponseDto>> init() {
		return ResponseEntity.ok(this.userDeviceService.registerDevice());
	}

//	@PostMapping(value = "/authentication/refresh-claims-token")
//	public ResponseEntity<?> getRefreshToken(@RequestBody RefreshClaimRequestDto refreshClaimRequest) throws Exception{
//		return jwtAuthenticationService.generateRefreshToken(refreshClaimRequest);
//	}
//
//	@GetMapping(value = "/authentication/isExist", params = {"phone","email"})
//	public ResponseEntity<?> isContactDetailsExist(@RequestParam String phone, @RequestParam String email){
//		return jwtAuthenticationService.isContactInfoExist(email, phone);
//	}

	@PostMapping(value = "/login")
	public ResponseEntity<?> login(@RequestHeader("AuthType") String authType, @RequestHeader("Provider") String provider, @RequestHeader("Operation") IdentityProviderOperation operation, @RequestBody AbstractAuthRequest authRequest) {
		return ResponseEntity.ok(this.identityService.authenticate(provider, authType, operation, authRequest));
	}

	@PostMapping(value = "/logout")
	public ResponseEntity<?> logout(@RequestHeader(ApplicationConstants.X_IDENTITY_PROVIDER) String provider) {
		return ResponseEntity.ok(this.identityService.logout(provider));
	}

	@PostMapping(value = "/signup")
	@ActionInfo(id = "SIGN_UP_USER", name = "Sign up user", accessLevel = AccessLevel.ADMIN, description = "User self registration")
	public ResponseEntity<ApiResponseDto<?>> saveUser(@RequestBody UserRequestDto user) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.selfRegistration(user));
	}

	@PostMapping(value = "/activate-user")
	public ResponseEntity<?> activateUser(@RequestBody ActivateUserAuthenticationDto activateUserAuthentication) {
		return ResponseEntity.ok(this.identityService.activateUser(activateUserAuthentication));
	}

	@PostMapping(value = "/change-user-password")
	@ActionInfo(id = "CHANGE_USER_PASSWORD", name = "Change user password", accessLevel = AccessLevel.OWNER, description = "Change user password")
	public ResponseEntity<?> changeUserPassword(@RequestBody ActivateUserAuthenticationDto activateUserAuthentication) {
		return ResponseEntity.ok(this.identityService.changeUserPassword(activateUserAuthentication));
	}

	@GetMapping(value = "/request-for-temp-password")
	@ActionInfo(id = "REQUEST_FOR_TEMP_PASSWORD", name = "Request for temp password", accessLevel = AccessLevel.ADMIN, description = "Request to generate temp user password")
	public ResponseEntity<?> requestForTempPassword(@RequestParam String userName) {
		return ResponseEntity.ok(this.identityService.requestToSendTempPassword(userName));
	}

}