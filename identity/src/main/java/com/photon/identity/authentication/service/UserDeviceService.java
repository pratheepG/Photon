package com.photon.identity.authentication.service;

import com.photon.constants.ApplicationConstants;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.response.UserDeviceRegistrationResponseDto;
import com.photon.identity.authentication.entity.RefreshToken;
import com.photon.identity.authentication.entity.UserDevice;
import com.photon.identity.commons.grpc.client.DeviceSyncService;
import com.photon.identity.authentication.repository.UserDeviceRepository;
import com.photon.utils.HttpRequestManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author pratheepg
 */
@Slf4j
@Service
public class UserDeviceService {

	private final UserDeviceRepository userDeviceRepository;
	private final DeviceSyncService deviceSyncService;
	private final HttpServletRequest request;

    public UserDeviceService(UserDeviceRepository userDeviceRepository, DeviceSyncService deviceSyncService, HttpServletRequest request) {
        this.userDeviceRepository = userDeviceRepository;
        this.deviceSyncService = deviceSyncService;
        this.request = request;
    }

    public Optional<UserDevice> findByUserId(String userId) {
		return userDeviceRepository.findByUserId(userId);
	}

	public Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken){
		return userDeviceRepository.findById(refreshToken.getUserDevice().getId());
	}

	public Optional<UserDevice> findByDeviceId(String deviceId){
		return userDeviceRepository.findByDeviceId(deviceId);
	}

	public void delete(UserDevice userDevice){
		userDeviceRepository.delete(userDevice);
	}

	public UserDevice save(UserDevice userDevice){
		return userDeviceRepository.save(userDevice);
	}

	@Transactional
	public ApiResponseDto<UserDeviceRegistrationResponseDto> registerDevice() throws ApplicationException {
		try {
			HttpSession session = this.request.getSession(true);
			log.info("HEADER PARAM : {}", HttpRequestManager.getCurrentHttpRequest().getHeader(ApplicationConstants.REPORTING_PARAMS));
			ReportingParamsDto reportingParams = HttpRequestManager.getReportingParams();
			UserDevice userDevice = UserDevice.builder()
					.deviceType(reportingParams.getDeviceType()).deviceId(reportingParams.getDeviceId())
					.isActive(true).isRefreshActive(false).userId(null).refreshToken(null).build();

			Optional<UserDevice> existingDevice = this.userDeviceRepository.findByDeviceId(userDevice.getDeviceId());

			if (existingDevice.isEmpty()) {
				UserDevice res = this.userDeviceRepository.save(userDevice);
				if (!Objects.isNull(res.getId())) {
					this.deviceSyncService.registerNewDevice(reportingParams);
					UserDeviceRegistrationResponseDto userDeviceRegistrationResponse = UserDeviceRegistrationResponseDto.builder().id(res.getId()).build();
					userDeviceRegistrationResponse.setSessionId(session.getId());
					return SuccessEnum.CREATED.getSuccessResponseBody(userDeviceRegistrationResponse);
				} else {
					throw new ApplicationException(ExceptionEnum.ERR_1027.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
				}
			}
			UserDeviceRegistrationResponseDto userDeviceRegistrationResponse = UserDeviceRegistrationResponseDto.builder().id(existingDevice.get().getId()).build();
			userDeviceRegistrationResponse.setSessionId(session.getId());
			return SuccessEnum.CREATED.getSuccessResponseBody(userDeviceRegistrationResponse);
		} catch (ApplicationException ex) {
		    throw ex;
	    } catch (Exception e) {
			throw new ApplicationException(ExceptionEnum.ERR_1027.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void verifyRefreshToken(RefreshToken refreshToken) {
		UserDevice userDevice = findByRefreshToken(refreshToken).orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody("No device found for the matching token. Please login again"), HttpStatus.EXPECTATION_FAILED));
		
		if(!userDevice.getIsRefreshActive()) {
			throw new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody("Refresh blocked for the device. Please login through a different device"), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
}