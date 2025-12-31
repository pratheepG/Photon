package com.photon.identity.authentication.service;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.request.RoleFeatureActionRequestDto;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.RoleDto;
import com.photon.identity.authentication.dto.mapper.RoleMapper;
import com.photon.identity.authentication.dto.response.RoleResponseDto;
import com.photon.identity.authentication.entity.Role;
import com.photon.identity.authentication.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public ApiResponseDto<RoleResponseDto> findRoleById(Long id) throws ApplicationException {
        try {
            log.debug("findRoleById input param Id : {}", id);
            Optional<RoleResponseDto> rolePojoOptional = this.roleRepository.findById(id).map(RoleResponseDto::toDto);
            if (rolePojoOptional.isPresent()) {
                return SuccessEnum.SUCCESS.getSuccessResponseBody(rolePojoOptional.get());
            } else {
                log.error("Role not found for ID: {}", id);
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND);
            }
        } catch (ApplicationException ae) {
            log.error("ApplicationException in findRoleById: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("General Exception in findRoleById: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> createNewRole(RoleDto roleDto) throws ApplicationException {
        try {
            log.debug("createNewRole input param : {}", roleDto.toString());
            Role role = this.roleRepository.save(RoleMapper.toEntity(roleDto));
            if (Objects.nonNull(role.getId()))
                return SuccessEnum.CREATED.getSuccessResponseBody();
            else
                throw new ApplicationException(ExceptionEnum.ERR_1010.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in createNewRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in createNewRole : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> updateRole(RoleDto roleDto) throws ApplicationException {
        try {
            log.debug("updateRole input param: {}", roleDto.toString());
            if(Objects.isNull(roleDto.getId()))
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody("Role Id can't be empty"), HttpStatus.BAD_REQUEST);

            Role role = this.roleRepository.save(RoleMapper.toEntity(roleDto));
            if (Objects.nonNull(role.getId()))
                return SuccessEnum.CREATED.getSuccessResponseBody();
            else
                throw new ApplicationException(ExceptionEnum.ERR_1010.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in updateRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in updateRole : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> patchRole(Long id, RoleDto roleDto) throws ApplicationException {
        try {
            log.debug("patchRole input param : {}", roleDto.toString());
            Optional<Role> existingRole = this.roleRepository.findById(id);

            if (existingRole.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("role not found for the given Id"), HttpStatus.NOT_FOUND);

            Role roleEntity = RoleMapper.partialUpdate(roleDto, existingRole.get());

            Role role = this.roleRepository.save(roleEntity);
            if (Objects.nonNull(role.getId()))
                return SuccessEnum.UPDATED.getSuccessResponseBody();
            else
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in patchRole : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in patchRole : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> addFeatureActionInRole(Long id, Map<String, Set<String>> featureActions) throws ApplicationException {
        try {
            log.debug("addFeatureActionInRole input param : {}", featureActions.toString());
            Optional<Role> existingRole = this.roleRepository.findById(id);

            if (existingRole.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("role not found for the given Id"), HttpStatus.NOT_FOUND);

            Role roleEntity = existingRole.get();

            featureActions.forEach((key, value) -> {
                if(roleEntity.getFeatureActions().get(key).isEmpty())
                    roleEntity.getFeatureActions().put(key, value);
                else
                    roleEntity.getFeatureActions().get(key).addAll(value);
            });

            Role role = this.roleRepository.save(roleEntity);
            if (Objects.nonNull(role.getId()))
                return SuccessEnum.UPDATED.getSuccessResponseBody("Feature action added successfully");
            else
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in addFeatureActionInRole : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in addFeatureActionInRole : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
            log.error("Exception in addAllFeatureActionInRole: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> deleteFeatureActionFromRole(Long id, Map<String, Set<String>> featureActions) throws ApplicationException {
        try {
            log.debug("deleteFeatureActionFromRole input param : {}", featureActions.toString());
            Optional<Role> existingRole = this.roleRepository.findById(id);

            if (existingRole.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Role not found for the given Id"), HttpStatus.NOT_FOUND);

            Role roleEntity = existingRole.get();

            featureActions.forEach((key, value) -> {
                if(!roleEntity.getFeatureActions().get(key).isEmpty())
                    roleEntity.getFeatureActions().get(key).removeAll(value);

                if(roleEntity.getFeatureActions().get(key).isEmpty())
                    roleEntity.getFeatureActions().remove(key);
            });

            Role role = this.roleRepository.save(roleEntity);
            if (Objects.nonNull(role.getId()))
                return SuccessEnum.DELETED.getSuccessResponseBody("Feature action deleted successfully");
            else
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteFeatureActionFromRole : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in deleteFeatureActionFromRole : {} ",e.getMessage());
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

    public ApiResponseDto<?> getRoles(AccessLevel accessLevel, int pageNumber, int pageSize) throws ApplicationException {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            List<AccessLevel> accessLevels = new ArrayList<>();
            switch (accessLevel){
                case ADMIN -> {
                    accessLevels.add(AccessLevel.ADMIN);
                }
                case TENANT_ADMIN -> {
                    accessLevels.add(AccessLevel.ADMIN);
                    accessLevels.add(AccessLevel.TENANT_ADMIN);
                }
                case OWNER -> {
                    accessLevels.add(AccessLevel.OWNER);
                    accessLevels.add(AccessLevel.ADMIN);
                    accessLevels.add(AccessLevel.TENANT_ADMIN);
                }
                case EDITOR -> {
                    accessLevels.add(AccessLevel.EDITOR);
                    accessLevels.add(AccessLevel.OWNER);
                    accessLevels.add(AccessLevel.ADMIN);
                    accessLevels.add(AccessLevel.TENANT_ADMIN);
                }
                case VIEWER -> {
                    accessLevels.add(AccessLevel.VIEWER);
                    accessLevels.add(AccessLevel.EDITOR);
                    accessLevels.add(AccessLevel.OWNER);
                    accessLevels.add(AccessLevel.ADMIN);
                    accessLevels.add(AccessLevel.TENANT_ADMIN);
                }
            }

            Page<Role> roles = this.roleRepository.findAllByAccessLevelIn(accessLevels, pageable);
            Map<String, List<Map<Long, String>>> grouped = roles.getContent().stream()
                    .collect(Collectors.groupingBy(
                            Role::getIdp,
                            Collectors.mapping(
                                    r -> Collections.singletonMap(r.getId(), r.getRoleId().concat("|").concat(r.getAccessLevel().name())),
                                    Collectors.toList()
                            )
                    ));

            return SuccessEnum.SUCCESS.getSuccessResponseBody(grouped).page(pageable.getPageNumber())
                            .size(roles.getNumberOfElements()).totalPages(roles.getTotalPages()).totalRecords((int) roles.getTotalElements());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAllRoles : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getRoles : {} ", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<RoleResponseDto>> getAllRoles(int pageNumber, int pageSize) throws ApplicationException {
        try{
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<RoleResponseDto> roles = this.roleRepository.findAll(pageable).map(RoleResponseDto::toDto);
            return SuccessEnum.SUCCESS.getSuccessResponseBody(roles.stream().toList()).page(pageable.getPageNumber()).size(roles.stream().toList().size())
                    .totalRecords((int) roles.getTotalElements()).totalPages(roles.getTotalPages());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAllRoles : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in getAllRoles : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> deleteRolesByIds(List<Long> ids) throws ApplicationException {
        try{
            if(this.roleRepository.findAllById(ids).isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.BAD_REQUEST);

            this.roleRepository.deleteAllById(ids);

            if(!this.roleRepository.findAllById(ids).isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1011.getErrorResponseBody(), HttpStatus.BAD_REQUEST);

            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteRolesByIds : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in deleteRolesByIds : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}