package com.photon.identity.idp.dto.mapper;

import com.photon.identity.idp.dto.*;
import com.photon.identity.idp.dto.request.IdentityProviderRequestDto;
import com.photon.identity.idp.entity.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityProviderMapper {

    public static IdentityProviderDto toDto(IdentityProvider identityProvider) {
        if (identityProvider == null) {
            return null;
        }

        return new IdentityProviderDto(
                identityProvider.getId(),
                identityProvider.getName(),
                identityProvider.getDescription(),
                identityProvider.getIsActive(),
                identityProvider.getIdpDefaultRole(),
                identityProvider.getIdentityProviderType(),
                identityProvider.getSignatureAlgorithm(),
                CertificateMapper.toDto(identityProvider.getCertificate()),
                identityProvider.getSessionTimeoutMinutes(),
                identityProvider.getSessionIdleTimeoutMinutes(),
                identityProvider.getActiveSessions(),
                identityProvider.getRefreshTokenSessionTimeoutMinutes(),
                identityProvider.getIdentityProviderAuthTypes().stream()
                        .map(IdentityProviderAuthTypeMapper::toDto)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                identityProvider.getIsMfaEnabled(),
                identityProvider.getIsMfaRequiredForEveryLogin(),
                identityProvider.getIsMfaConditionCheckEnabled(),
                identityProvider.getIncludeX509InJwt(),
                identityProvider.getLastValidateMfaExpiresInMinutes(),
                toDto(identityProvider.getMfaCondition())
        );
    }

    public static IdentityProvider toEntity(IdentityProviderDto identityProviderDto) {
        if (identityProviderDto == null) {
            return null;
        }

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId(identityProviderDto.getId());
        identityProvider.setName(identityProviderDto.getName());
        identityProvider.setDescription(identityProviderDto.getDescription());
        identityProvider.setIsActive(identityProviderDto.getIsActive());
        identityProvider.setIdpDefaultRole(identityProviderDto.getIdpDefaultRole());
        identityProvider.setIdentityProviderType(identityProviderDto.getIdentityProviderType());
        identityProvider.setSignatureAlgorithm(identityProviderDto.getSignatureAlgorithm());
        identityProvider.setCertificate(CertificateMapper.toEntity(identityProviderDto.getCertificate()));
        identityProvider.setSessionTimeoutMinutes(identityProviderDto.getSessionTimeoutMinutes());
        identityProvider.setSessionIdleTimeoutMinutes(identityProviderDto.getSessionIdleTimeoutMinutes());
        identityProvider.setActiveSessions(identityProviderDto.getActiveSessions());
        identityProvider.setRefreshTokenSessionTimeoutMinutes(identityProviderDto.getRefreshTokenSessionTimeoutMinutes());

        if (identityProviderDto.getIdentityProviderAuthTypes() != null) {
            identityProvider.setIdentityProviderAuthTypes(identityProviderDto.getIdentityProviderAuthTypes().stream()
                    .map(IdentityProviderAuthTypeMapper::toEntity)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        identityProvider.setIsMfaEnabled(identityProviderDto.getIsMfaEnabled());
        identityProvider.setIsMfaRequiredForEveryLogin(identityProviderDto.getIsMfaRequiredForEveryLogin());
        identityProvider.setIsMfaConditionCheckEnabled(identityProviderDto.getIsMfaConditionCheckEnabled());
        identityProvider.setLastValidateMfaExpiresInMinutes(identityProviderDto.getLastValidateMfaExpiresInMinutes());
        identityProvider.setMfaCondition(toEntity(identityProviderDto.getMfaCondition()));
        identityProvider.setIncludeX509InJwt(identityProviderDto.getIncludeX509InJwt());

        return identityProvider;
    }

    public static IdentityProvider toEntity(IdentityProviderRequestDto identityProviderDto) {
        if (identityProviderDto == null) {
            return null;
        }

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId(identityProviderDto.getId());
        identityProvider.setName(identityProviderDto.getName());
        identityProvider.setDescription(identityProviderDto.getDescription());
        identityProvider.setIdentityProviderType(identityProviderDto.getIdentityProviderType());
        identityProvider.setSignatureAlgorithm(identityProviderDto.getSignatureAlgorithm());
        identityProvider.setSessionTimeoutMinutes(identityProviderDto.getSessionTimeoutMinutes());
        identityProvider.setSessionIdleTimeoutMinutes(identityProviderDto.getSessionIdleTimeoutMinutes());
        identityProvider.setActiveSessions(identityProviderDto.getActiveSessions());
        identityProvider.setLastValidateMfaExpiresInMinutes(identityProviderDto.getLastValidateMfaExpiresInMinutes());

        if (identityProviderDto.getRefreshTokenSessionTimeoutMinutes() != null) {
            identityProvider.setRefreshTokenSessionTimeoutMinutes(identityProviderDto.getRefreshTokenSessionTimeoutMinutes());
        } else {
            identityProvider.setRefreshTokenSessionTimeoutMinutes(-1);
        }
        if (identityProviderDto.getIdpDefaultRole() != null) {
            identityProvider.setIdpDefaultRole(identityProviderDto.getIdpDefaultRole());
        }
        if (identityProviderDto.getIsActive() != null) {
            identityProvider.setIsActive(identityProviderDto.getIsActive());
        } else {
            identityProvider.setIsActive(false);
        }
        if (identityProviderDto.getIncludeX509InJwt() != null) {
            identityProvider.setIncludeX509InJwt(identityProviderDto.getIncludeX509InJwt());
        } else {
            identityProvider.setIncludeX509InJwt(false);
        }
        if (identityProviderDto.getIsMfaRequiredForEveryLogin() != null) {
            identityProvider.setIsMfaRequiredForEveryLogin(identityProviderDto.getIsMfaRequiredForEveryLogin());
        } else {
            identityProvider.setIsMfaRequiredForEveryLogin(false);
        }
        if (identityProviderDto.getIsMfaEnabled() != null) {
            identityProvider.setIsMfaEnabled(identityProviderDto.getIsMfaEnabled());
        } else {
            identityProvider.setIsMfaEnabled(false);
        }
        if (identityProviderDto.getIsMfaConditionCheckEnabled() != null) {
            identityProvider.setIsMfaConditionCheckEnabled(identityProviderDto.getIsMfaConditionCheckEnabled());
        } else {
            identityProvider.setIsMfaConditionCheckEnabled(false);
        }
        if (identityProviderDto.getLastValidateMfaExpiresInMinutes() != null) {
            identityProvider.setLastValidateMfaExpiresInMinutes(identityProviderDto.getLastValidateMfaExpiresInMinutes());
        } else {
            identityProvider.setLastValidateMfaExpiresInMinutes(-1L);
        }

        return identityProvider;
    }

    public static IdentityProvider partialUpdate(IdentityProviderRequestDto identityProviderDto, IdentityProvider identityProvider) {
        if (identityProviderDto == null) {
            return null;
        }

        if (identityProviderDto.getRefreshTokenSessionTimeoutMinutes() != null) {
            identityProvider.setRefreshTokenSessionTimeoutMinutes(identityProviderDto.getRefreshTokenSessionTimeoutMinutes());
        } else {
            identityProvider.setRefreshTokenSessionTimeoutMinutes(-1);
        }
        if (identityProviderDto.getIdpDefaultRole() != null) {
            identityProvider.setIdpDefaultRole(identityProviderDto.getIdpDefaultRole());
        }
        if (identityProviderDto.getName() != null) {
            identityProvider.setName(identityProviderDto.getName());
        }
        if (identityProviderDto.getDescription() != null) {
            identityProvider.setDescription(identityProviderDto.getDescription());
        }
        if (identityProviderDto.getIdentityProviderType() != null) {
            identityProvider.setIdentityProviderType(identityProviderDto.getIdentityProviderType());
        }
        if (identityProviderDto.getSignatureAlgorithm() != null) {
            identityProvider.setSignatureAlgorithm(identityProviderDto.getSignatureAlgorithm());
        }
        if (identityProviderDto.getSessionTimeoutMinutes() != null) {
            identityProvider.setSessionTimeoutMinutes(identityProviderDto.getSessionTimeoutMinutes());
        }
        if (identityProviderDto.getSessionIdleTimeoutMinutes() != null) {
            identityProvider.setSessionIdleTimeoutMinutes(identityProviderDto.getSessionIdleTimeoutMinutes());
        }
        if (identityProviderDto.getActiveSessions() != null) {
            identityProvider.setActiveSessions(identityProviderDto.getActiveSessions());
        }
        if (identityProviderDto.getIsActive() != null) {
            identityProvider.setIsActive(identityProviderDto.getIsActive());
        } else {
            identityProvider.setIsActive(false);
        }
        if (identityProviderDto.getIncludeX509InJwt() != null) {
            identityProvider.setIncludeX509InJwt(identityProviderDto.getIncludeX509InJwt());
        } else {
            identityProvider.setIncludeX509InJwt(false);
        }
        if (identityProviderDto.getIsMfaRequiredForEveryLogin() != null) {
            identityProvider.setIsMfaRequiredForEveryLogin(identityProviderDto.getIsMfaRequiredForEveryLogin());
        } else {
            identityProvider.setIsMfaRequiredForEveryLogin(false);
        }
        if (identityProviderDto.getIsMfaEnabled() != null) {
            identityProvider.setIsMfaEnabled(identityProviderDto.getIsMfaEnabled());
        } else {
            identityProvider.setIsMfaEnabled(false);
        }
        if (identityProviderDto.getIsMfaConditionCheckEnabled() != null) {
            identityProvider.setIsMfaConditionCheckEnabled(identityProviderDto.getIsMfaConditionCheckEnabled());
        } else {
            identityProvider.setIsMfaConditionCheckEnabled(false);
        }
        if (identityProviderDto.getLastValidateMfaExpiresInMinutes() != null) {
            identityProvider.setLastValidateMfaExpiresInMinutes(identityProviderDto.getLastValidateMfaExpiresInMinutes());
        } else {
            identityProvider.setLastValidateMfaExpiresInMinutes(-1L);
        }

        return identityProvider;
    }

    public static IdentityProvider partialUpdate(IdentityProviderDto identityProviderDto, IdentityProvider identityProvider) {
        if (identityProviderDto == null || identityProvider == null) {
            return identityProvider;
        }

        if (identityProviderDto.getRefreshTokenSessionTimeoutMinutes() != null) {
            identityProvider.setRefreshTokenSessionTimeoutMinutes(identityProviderDto.getRefreshTokenSessionTimeoutMinutes());
        } else {
            identityProvider.setRefreshTokenSessionTimeoutMinutes(-1);
        }
        if (identityProviderDto.getId() != null) {
            identityProvider.setId(identityProviderDto.getId());
        }
        if (identityProviderDto.getIdpDefaultRole() != null) {
            identityProvider.setIdpDefaultRole(identityProviderDto.getIdpDefaultRole());
        }
        if (identityProviderDto.getName() != null) {
            identityProvider.setName(identityProviderDto.getName());
        }
        if (identityProviderDto.getDescription() != null) {
            identityProvider.setDescription(identityProviderDto.getDescription());
        }
        if (identityProviderDto.getIsActive() != null) {
            identityProvider.setIsActive(identityProviderDto.getIsActive());
        } else {
            identityProvider.setIsActive(false);
        }
        if (identityProviderDto.getIncludeX509InJwt() != null) {
            identityProvider.setIncludeX509InJwt(identityProviderDto.getIncludeX509InJwt());
        } else {
            identityProvider.setIncludeX509InJwt(false);
        }
        if (identityProviderDto.getIdentityProviderType() != null) {
            identityProvider.setIdentityProviderType(identityProviderDto.getIdentityProviderType());
        }
        if (identityProviderDto.getSignatureAlgorithm() != null) {
            identityProvider.setSignatureAlgorithm(identityProviderDto.getSignatureAlgorithm());
        }
        if (identityProviderDto.getCertificate() != null) {
            identityProvider.setCertificate(CertificateMapper.toEntity(identityProviderDto.getCertificate()));
        }
        if (identityProviderDto.getSessionTimeoutMinutes() != null) {
            identityProvider.setSessionTimeoutMinutes(identityProviderDto.getSessionTimeoutMinutes());
        }
        if (identityProviderDto.getSessionIdleTimeoutMinutes() != null) {
            identityProvider.setSessionIdleTimeoutMinutes(identityProviderDto.getSessionIdleTimeoutMinutes());
        }
        if (identityProviderDto.getActiveSessions() != null) {
            identityProvider.setActiveSessions(identityProviderDto.getActiveSessions());
        }
        if (identityProviderDto.getIdentityProviderAuthTypes() != null) {
            identityProvider.setIdentityProviderAuthTypes(identityProviderDto.getIdentityProviderAuthTypes().stream()
                    .map(IdentityProviderAuthTypeMapper::toEntity)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        if (identityProviderDto.getIsMfaEnabled() != null) {
            identityProvider.setIsMfaEnabled(identityProviderDto.getIsMfaEnabled());
        }
        if (identityProviderDto.getIsMfaRequiredForEveryLogin() != null) {
            identityProvider.setIsMfaRequiredForEveryLogin(identityProviderDto.getIsMfaRequiredForEveryLogin());
        } else {
            identityProvider.setIsMfaRequiredForEveryLogin(false);
        }
        if (identityProviderDto.getIsMfaConditionCheckEnabled() != null) {
            identityProvider.setIsMfaConditionCheckEnabled(identityProviderDto.getIsMfaConditionCheckEnabled());
        } else {
            identityProvider.setIsMfaConditionCheckEnabled(false);
        }
        if (identityProviderDto.getLastValidateMfaExpiresInMinutes() != null) {
            identityProvider.setLastValidateMfaExpiresInMinutes(identityProviderDto.getLastValidateMfaExpiresInMinutes());
        } else {
            identityProvider.setLastValidateMfaExpiresInMinutes(-1L);
        }
        if (identityProviderDto.getMfaCondition() != null) {
            identityProvider.setMfaCondition(toEntity(identityProviderDto.getMfaCondition()));
        }

        return identityProvider;
    }




    // ------------------- MFA Condition Mappings ------------------- //

    public static MFAConditionSetDto toDto(MFAConditionSet conditionSet) {
        if (conditionSet == null) {
            return null;
        }

        return new MFAConditionSetDto(
                conditionSet.getId(),
                conditionSet.getName(),
                conditionSet.getDescription(),
                conditionSet.getGroups().stream()
                        .map(IdentityProviderMapper::toDto)
                        .collect(Collectors.toSet())
        );
    }

    public static MFAConditionSet toEntity(MFAConditionSetDto conditionSetDto) {
        if (conditionSetDto == null) {
            return null;
        }

        MFAConditionSet conditionSet = new MFAConditionSet();
        conditionSet.setName(conditionSetDto.getName());
        conditionSet.setDescription(conditionSetDto.getDescription());

        Set<MFAConditionGroup> groups = conditionSetDto.getGroups().stream()
                .map(IdentityProviderMapper::toEntity)
                .peek(group -> group.setConditionSet(conditionSet))
                .collect(Collectors.toSet());
        conditionSet.setGroups(groups);

        return conditionSet;
    }

    public static MFAConditionGroupDto toDto(MFAConditionGroup group) {
        if (group == null) {
            return null;
        }

        return new MFAConditionGroupDto(
                group.getId(),
                group.getOperator(),
                group.getItems().stream()
                        .map(IdentityProviderMapper::toDto)
                        .collect(Collectors.toSet())
        );
    }

    public static MFAConditionGroup toEntity(MFAConditionGroupDto groupDto) {
        if (groupDto == null) {
            return null;
        }

        MFAConditionGroup group = new MFAConditionGroup();
        group.setOperator(groupDto.getOperator());

        Set<MFAConditionGroupItem> items = groupDto.getItems().stream()
                .map(IdentityProviderMapper::toEntity)
                .peek(item -> item.setGroup(group))
                .collect(Collectors.toSet());
        group.setItems(items);

        return group;
    }

    public static MFAConditionGroupItemDto toDto(MFAConditionGroupItem item) {
        if (item == null) {
            return null;
        }

        return new MFAConditionGroupItemDto(
                item.getId(),
                item.getOperator(),
                item.getCondition()
        );
    }

    public static MFAConditionGroupItem toEntity(MFAConditionGroupItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        MFAConditionGroupItem item = new MFAConditionGroupItem();
        item.setOperator(itemDto.getOperator());
        item.setCondition(itemDto.getCondition());

        return item;
    }
}