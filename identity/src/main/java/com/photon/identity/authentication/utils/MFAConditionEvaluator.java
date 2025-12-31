package com.photon.identity.authentication.utils;

import com.photon.dto.ReportingParamsDto;
import com.photon.identity.authentication.dto.UserDeviceDto;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.dto.MFAConditionGroupDto;
import com.photon.identity.idp.dto.MFAConditionGroupItemDto;
import com.photon.identity.idp.dto.MFAConditionSetDto;
import org.springframework.stereotype.Service;

@Service
public class MFAConditionEvaluator {

    /**
     * Evaluate the MFAConditionSet based on the provided user and request details.
     *
     * @param conditionSet        The MFAConditionSet to evaluate.
     * @param userDto             The user information.
     * @param userDeviceDto       The device information.
     * @param reportingParamsDto The reportingParams request context.
     * @return True if MFA is required, False otherwise.
     */
    public boolean evaluate(MFAConditionSetDto conditionSet, UserDto userDto, UserDeviceDto userDeviceDto, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto) {
        for (MFAConditionGroupDto group : conditionSet.getGroups()) {
            boolean groupResult = evaluateGroup(group, userDto, userDeviceDto, reportingParamsDto, identityProviderDto);

            if ("AND".equalsIgnoreCase(group.getOperator()) && !groupResult) {
                return false; // All "AND" conditions must be true
            }
            if ("OR".equalsIgnoreCase(group.getOperator()) && groupResult) {
                return true; // Any "OR" condition is enough
            }
        }
        return true; // Default is to trigger MFA
    }

    /**
     * Evaluate an MFAConditionGroup.
     */
    private boolean evaluateGroup(MFAConditionGroupDto group, UserDto userDto, UserDeviceDto userDeviceDto, ReportingParamsDto reportingParamsDto, IdentityProviderDto identityProviderDto) {
        boolean result = false;

        for (MFAConditionGroupItemDto item : group.getItems()) {
            boolean conditionResult = item.getCondition().evaluate(userDto, userDeviceDto, reportingParamsDto, identityProviderDto);

            if ("AND".equalsIgnoreCase(item.getOperator())) {
                result = result && conditionResult;
            } else if ("OR".equalsIgnoreCase(item.getOperator())) {
                result = result || conditionResult;
            }
        }

        return result;
    }
}