package com.photon.identity.authentication.dto.mapper;

import com.photon.identity.authentication.dto.AddressDto;
import com.photon.identity.authentication.dto.ElectronicAddressDto;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.dto.request.UserByAdminRequestDto;
import com.photon.identity.authentication.entity.Address;
import com.photon.identity.authentication.entity.ElectronicAddress;
import com.photon.identity.authentication.entity.Tenant;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.commons.dto.mapper.CdnAssetInfoMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getUserId(),
                user.getTenants().stream().map(Tenant::getTenantId).collect(Collectors.toSet()),
                user.getSubscriberId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getCountryCode(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getSex(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.getInValidLoginAttempts(),
                user.getInValidLoginAttemptAuthType(),
                user.getDob(),
                user.getCreatedOn(),
                user.getUpdatedOn(),
                user.getLastMfaVerifiedOn(),
                user.getLastPasswordUpdatedOn(),
                user.getIsServerConsoleUser(),
                user.getIsMfaEnabled(),
                user.getRoles().stream()
                        .map(RoleMapper::toDto)
                        .collect(Collectors.toSet()),
                user.getAddress().stream()
                        .map(AddressMapper::toDto)
                        .collect(Collectors.toSet()),
                user.getElectronicAddress().stream()
                        .map(ElectronicAddressMapper::toDto)
                        .collect(Collectors.toSet()),
                user.getActiveAuthAdapters(),
                null,
                user.getAdditionalAttributes()
        );

        //CdnAssetInfoMapper.toDto(user.getProfilePic())
    }

    public static User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setSubscriberId(userDto.getSubscriberId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUserName(userDto.getUserName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setCountryCode(userDto.getCountryCode());
        user.setEmail(userDto.getEmail());
        user.setSex(userDto.getSex());
        user.setIsEnabled(userDto.getIsEnabled());
        user.setDob(userDto.getDob());
        user.setCreatedOn(userDto.getCreatedOn());
        user.setUpdatedOn(userDto.getUpdatedOn());
        user.setLastMfaVerifiedOn(userDto.getLastMfaVerifiedOn());
        user.setLastPasswordUpdatedOn(userDto.getLastPasswordUpdatedOn());
        user.setIsServerConsoleUser(userDto.getIsServerConsoleUser());
        user.setIsMfaEnabled(userDto.getIsMfaEnabled());
        user.setActiveAuthAdapters(userDto.getActiveAuthAdapters());

        user.setInValidLoginAttempts(userDto.getInValidLoginAttempts());
        user.setInValidLoginAttemptAuthType(userDto.getInValidLoginAttemptAuthType());

        if (userDto.getAddress() != null) {
            user.setAddress(userDto.getAddress().stream()
                    .map(AddressMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        if (userDto.getElectronicAddress() != null) {
            user.setElectronicAddress(userDto.getElectronicAddress().stream()
                    .map(ElectronicAddressMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        if (userDto.getRoles() != null) {
            user.setRoles(userDto.getRoles().stream()
                    .map(RoleMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        if(userDto.getProfilePic() != null) {
            user.setProfilePic(CdnAssetInfoMapper.toEntity(userDto.getProfilePic()));
        }
        if (userDto.getAdditionalAttributes()!= null) {
            user.setAdditionalAttributes(userDto.getAdditionalAttributes());
        }

        return user;
    }

    public static User partialUpdate(UserDto userDto, User user) {
        if (userDto == null || user == null) {
            return user;
        }

        if (userDto.getUserId() != null) {
            user.setUserId(userDto.getUserId());
        }
        if (userDto.getSubscriberId() != null) {
            user.setSubscriberId(userDto.getSubscriberId());
        }
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getUserName() != null) {
            user.setUserName(userDto.getUserName());
        }
        if (userDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userDto.getPhoneNumber());
        }
        if (userDto.getCountryCode() != null) {
            user.setCountryCode(userDto.getCountryCode());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getSex() != null) {
            user.setSex(userDto.getSex());
        }
        if (userDto.getIsEnabled() != null) {
            user.setIsEnabled(userDto.getIsEnabled());
        }
        if (userDto.getDob() != null) {
            user.setDob(userDto.getDob());
        }
        if (userDto.getCreatedOn() != null) {
            user.setCreatedOn(userDto.getCreatedOn());
        }
        if (userDto.getUpdatedOn() != null) {
            user.setUpdatedOn(userDto.getUpdatedOn());
        }
        if (userDto.getLastMfaVerifiedOn() != null) {
            user.setLastMfaVerifiedOn(userDto.getLastMfaVerifiedOn());
        }
        if (userDto.getAddress() != null) {
            user.setAddress(userDto.getAddress().stream()
                    .map(AddressMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        if (userDto.getElectronicAddress() != null) {
            user.setElectronicAddress(userDto.getElectronicAddress().stream()
                    .map(ElectronicAddressMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        if (userDto.getRoles() != null) {
            user.setRoles(userDto.getRoles().stream()
                    .map(RoleMapper::toEntity)
                    .collect(Collectors.toSet()));
        }
        if (userDto.getActiveAuthAdapters() != null) {
            user.setActiveAuthAdapters(userDto.getActiveAuthAdapters());
        }
        user.setInValidLoginAttempts(userDto.getInValidLoginAttempts());
        if (userDto.getInValidLoginAttemptAuthType() != null) {
            user.setInValidLoginAttemptAuthType(userDto.getInValidLoginAttemptAuthType());
        }
        if (userDto.getProfilePic()!= null) {
            user.setProfilePic(CdnAssetInfoMapper.toEntity(userDto.getProfilePic()));
        }
        if (userDto.getAdditionalAttributes()!= null) {
            user.setAdditionalAttributes(userDto.getAdditionalAttributes());
        }
        if (userDto.getLastPasswordUpdatedOn()!= null) {
            user.setLastPasswordUpdatedOn(userDto.getLastPasswordUpdatedOn());
        }
        if (userDto.getIsServerConsoleUser()!= null) {
            user.setIsServerConsoleUser(userDto.getIsServerConsoleUser());
        }

        return user;
    }

    public static User partialUpdate(UserByAdminRequestDto userDto, User user) {
        if (userDto == null || user == null) {
            return user;
        }

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getUserName() != null) {
            user.setUserName(userDto.getUserName());
        }
        if (userDto.getSex() != null) {
            user.setSex(userDto.getSex());
        }
        if (userDto.getDob() != null) {
            user.setDob(userDto.getDob());
        }
        if (userDto.getAddress() != null) {
            if (userDto.getAddress().isEmpty()) {
                user.getAddress().clear();
            } else {
                syncAddressesFromDto(user, userDto.getAddress());
            }
        }

        if (userDto.getElectronicAddress() != null) {
            if (userDto.getElectronicAddress().isEmpty()) {
                if (user.getElectronicAddress() != null) {
                    user.getElectronicAddress().clear();
                } else {
                    user.setElectronicAddress(new HashSet<>());
                }
            } else {
                syncElectronicAddressesFromDto(user, userDto.getElectronicAddress());
            }
        }

        if (userDto.getAdditionalAttributes()!= null) {
            user.setAdditionalAttributes(userDto.getAdditionalAttributes());
        }

        return user;
    }

    private static void syncAddressesFromDto(User user, Set<AddressDto> incomingDtos) {
        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }

        if (user.getAddress() == null) {
            user.setAddress(new HashSet<>());
        }

        Set<Address> existing = user.getAddress();

        if (incomingDtos == null || incomingDtos.isEmpty()) {
            return;
        }

        boolean hasDuplicateIds = incomingDtos.stream()
                .filter(dto -> dto.getId() != null)
                .map(AddressDto::getId)
                .collect(Collectors.toSet()).size()
                != incomingDtos.stream().filter(dto -> dto.getId() != null).count();

        if (hasDuplicateIds) {
            throw new IllegalArgumentException("Duplicate address ids in request");
        }

        Map<Integer, AddressDto> incomingById = incomingDtos.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(AddressDto::getId, Function.identity()));

        Iterator<Address> it = existing.iterator();
        while (it.hasNext()) {
            Address addr = it.next();
            Integer id = addr.getId();
            if (id == null || !incomingById.containsKey(id)) {
                it.remove();
            }
        }

        for (AddressDto dto : incomingDtos) {
            Integer dtoId = dto.getId();
            if (dtoId == null) {
                continue;
            }

            Optional<Address> maybe = existing.stream()
                    .filter(a -> a.getId() != null && a.getId().equals(dtoId))
                    .findFirst();

            if (maybe.isPresent()) {
                Address existingAddr = maybe.get();
                Address updated = AddressMapper.partialUpdate(dto, existingAddr);

                if (updated != existingAddr) {
                    boolean removed = existing.remove(existingAddr);
                    updated.setUser(user);
                    existing.add(updated);
                } else {
                    existingAddr.setUser(user);
                }
            } else {
                throw new IllegalArgumentException("Address id " + dtoId + " not found in user's existing addresses");
            }
        }

        incomingDtos.stream()
                .filter(dto -> dto.getId() == null)
                .forEach(dto -> {
                    Address newAddr = AddressMapper.toEntity(dto);
                    newAddr.setUser(user);
                    existing.add(newAddr);
                });
    }


    private static void syncElectronicAddressesFromDto(User user, Set<ElectronicAddressDto> incomingDtos) {
        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }

        if (user.getElectronicAddress() == null) {
            user.setElectronicAddress(new HashSet<>());
        }

        Set<ElectronicAddress> existing = user.getElectronicAddress();

        if (incomingDtos == null || incomingDtos.isEmpty()) {
            return;
        }

        boolean hasDuplicateIds = incomingDtos.stream()
                .filter(dto -> dto.getId() != null)
                .map(ElectronicAddressDto::getId)
                .collect(Collectors.toSet()).size()
                != incomingDtos.stream().filter(dto -> dto.getId() != null).count();

        if (hasDuplicateIds) {
            throw new IllegalArgumentException("Duplicate electronic address ids in request");
        }

        Map<Long, ElectronicAddressDto> incomingById = incomingDtos.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(ElectronicAddressDto::getId, Function.identity()));

        Iterator<ElectronicAddress> it = existing.iterator();
        while (it.hasNext()) {
            ElectronicAddress ea = it.next();
            Long id = ea.getId();
            if (id == null || !incomingById.containsKey(id)) {
                it.remove();
            }
        }

        for (ElectronicAddressDto dto : incomingDtos) {
            Long dtoId = dto.getId();
            if (dtoId == null) {
                continue;
            }

            Optional<ElectronicAddress> maybe = existing.stream()
                    .filter(e -> e.getId() != null && e.getId().equals(dtoId))
                    .findFirst();

            if (maybe.isPresent()) {
                ElectronicAddress existingEa = maybe.get();
                ElectronicAddress updated = ElectronicAddressMapper.partialUpdate(dto, existingEa);

                if (updated != existingEa) {
                    boolean removed = existing.remove(existingEa);
                    updated.setUser(user);
                    existing.add(updated);
                } else {
                    existingEa.setUser(user);
                }
            } else {
                throw new IllegalArgumentException("ElectronicAddress id " + dtoId + " not found in user's existing electronic addresses");

            }
        }

        incomingDtos.stream()
                .filter(dto -> dto.getId() == null)
                .forEach(dto -> {
                    ElectronicAddress newEa = ElectronicAddressMapper.toEntity(dto);
                    newEa.setUser(user);
                    existing.add(newEa);
                });
    }


}