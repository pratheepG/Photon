package com.photon.identity.commons.grpc.client;

import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.grpc.device.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class DeviceSyncService {

    @GrpcClient("alert")
    private DeviceInfoServiceGrpc.DeviceInfoServiceBlockingStub deviceServiceBlockingStub;

    //private final DeviceInfoServiceGrpc.DeviceInfoServiceBlockingStub deviceServiceBlockingStub;

//    public DeviceSyncService(DeviceInfoServiceGrpc.DeviceInfoServiceBlockingStub deviceServiceBlockingStub) {
//        this.deviceServiceBlockingStub = deviceServiceBlockingStub;
//    }

    public void linkDeviceToSubscriber(String subscriberId, String deviceId, String userId) throws ApplicationException {
        try {
            LinkDeviceRequest request = LinkDeviceRequest.newBuilder()
                    .setSubscriberId(subscriberId)
                    .setDeviceId(deviceId)
                    .setUserId(userId)
                    .build();

            LinkDeviceResponse response = deviceServiceBlockingStub.linkDeviceToSubscriber(request);
            log.info("Device Link Response: Status={}, Message={}", response.getStatus(), response.getMessage());

            if(!response.getStatus().equalsIgnoreCase("SUCCESS"))
                throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(response.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception ex) {
            log.error("gRPC call to link device failed: {}", ex.getMessage(), ex);
            throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void registerNewDevice(ReportingParamsDto reportingParams) throws ApplicationException {
        try {

            CreateDeviceInfoRequest request = CreateDeviceInfoRequest.newBuilder()
                    .setDeviceId(reportingParams.getDeviceId()).setDeviceType(reportingParams.getDeviceType())
                    .setPlatform(reportingParams.getDeviceOs()).setRegId("").setOsVersion(reportingParams.getOsVersion())
                    .setAppVersion(reportingParams.getApplicationId()).setDeviceModel(reportingParams.getDeviceName())
                    .setDeviceBrand("UNKNOWN").setBrowserVersion(reportingParams.getBrowserVersion()).setScreenResolution("UNKNOWN")
                    .setIpAddress(reportingParams.getIP()).setUserAgent(reportingParams.getUserAgent())
                    .build();

            CreateDeviceInfoResponse response = deviceServiceBlockingStub.createDeviceInfo(request);
            log.info("Create Device Response: Status={}, Message={}", response.getStatus(), response.getMessage());

            if(!response.getStatus().equalsIgnoreCase("SUCCESS"))
                throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(response.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (StatusRuntimeException ex) {
            log.error("gRPC call to link device failed: {}", ex.getMessage(), ex);
            throw new ApplicationException(ExceptionEnum.ERR_1045.getErrorResponseBody("Failed to communicate with Photon-Alert"), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception ex) {
            log.error("gRPC call to link device failed: {}", ex.getMessage(), ex);
            throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}