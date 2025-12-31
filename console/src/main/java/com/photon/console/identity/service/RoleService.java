package com.photon.console.identity.service;

import com.photon.console.identity.entity.Role;
import com.photon.console.identity.repository.RoleRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.dto.request.RoleFeatureActionRequestDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public ApiResponseDto<?> addAllFeatureActionInRole(List<RoleFeatureActionRequestDto> roleFeatureActionRequestList) throws ApplicationException {
        try {
            log.debug("addAllFeatureActionInRole input param: {}", roleFeatureActionRequestList.toString());
            Map<Long, RoleFeatureActionRequestDto> roleFeatureActionRequestMap = roleFeatureActionRequestList.stream().collect(Collectors.toMap(RoleFeatureActionRequestDto::getId, roleFeatureActionRequestDto -> roleFeatureActionRequestDto));
            List<Role> existingRole = this.roleRepository.findAllById(roleFeatureActionRequestMap.keySet());

            if (existingRole.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("role not found for the given Id"), HttpStatus.NOT_FOUND);

            Map<Long, Role> existingRoleMap = existingRole.stream().collect(Collectors.toMap(Role::getId, role -> role));

            roleFeatureActionRequestMap.forEach((id, featureActionRequest) ->
                    featureActionRequest.getFeatureActions().forEach((authType, featureActions) -> {
                        if( existingRoleMap.get(id) != null && existingRoleMap.get(id).getFeatureActions() != null && existingRoleMap.get(id).getFeatureActions().containsKey(authType))
                            existingRoleMap.get(id).getFeatureActions().get(authType).addAll(featureActions);
                        else
                            existingRoleMap.get(id).getFeatureActions().put(authType, featureActions);
                    }));

            this.roleRepository.saveAll(existingRoleMap.values());
            return SuccessEnum.UPDATED.getSuccessResponseBody("Feature action added successfully");
        } catch (ApplicationException ae) {
            log.error("ApplicationException in addAllFeatureActionInRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            e.printStackTrace();
            log.error("Exception in addAllFeatureActionInRole: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> deleteAllFeatureActionInRole(List<RoleFeatureActionRequestDto> roleFeatureActionRequestList) throws ApplicationException {
        try {
            log.debug("deleteAllFeatureActionInRole input param: {}", roleFeatureActionRequestList.toString());
            Map<Long, RoleFeatureActionRequestDto> roleFeatureActionRequestMap = roleFeatureActionRequestList.stream().collect(Collectors.toMap(RoleFeatureActionRequestDto::getId, roleFeatureActionRequestDto -> roleFeatureActionRequestDto));
            List<Role> existingRole = this.roleRepository.findAllById(roleFeatureActionRequestMap.keySet());

            if (existingRole.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("role not found for the given Id"), HttpStatus.NOT_FOUND);

            Map<Long, Role> existingRoleMap = existingRole.stream().collect(Collectors.toMap(Role::getId, role -> role));

            roleFeatureActionRequestMap.forEach((id, featureActionRequest) ->
                    featureActionRequest.getFeatureActions().forEach((appId, featureActions) -> {
                        Map<String, Set<String>> features = existingRoleMap.get(id).getFeatureActions();
                        if (features.containsKey(appId)) {
                            Set<String> featureSet = features.get(appId);
                            if (featureSet != null) {
                                featureSet.removeAll(featureActions);
                                if (featureSet.isEmpty()) {
                                    features.remove(appId);
                                }
                            }
                        }
                    })
            );

            this.roleRepository.saveAll(existingRoleMap.values());
            return SuccessEnum.UPDATED.getSuccessResponseBody("Feature action added successfully");
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteAllFeatureActionInRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in deleteAllFeatureActionInRole: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}