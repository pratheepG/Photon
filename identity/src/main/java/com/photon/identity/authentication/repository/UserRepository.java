package com.photon.identity.authentication.repository;

import com.photon.identity.authentication.dto.response.UserListDTO;
import com.photon.identity.authentication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String name);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumberAndCountryCode(String phoneNumber, String countryCode);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    Optional<List<User>> findByUserIdIn(Collection<String> ids);
    Page<User> findByIsServerConsoleUserTrue(Pageable pageable);
    Page<User> findByIsServerConsoleUserFalse(Pageable pageable);

    @Query("SELECT NEW com.photon.identity.authentication.dto.response.UserListDTO(u.userId, u.firstName, u.lastName, u.userName, u.phoneNumber, u.isEnabled, u.createdOn, r.name) " +
            "FROM User u LEFT JOIN u.roles r " +
            "WHERE u.isServerConsoleUser = false")
    Page<UserListDTO> findAllNonConsoleUserSummary(Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.isEnabled = :isEnabled WHERE u.userId = :userId")
    int updateEnabledStatus(@Param("userId") String userId, @Param("isEnabled") boolean isEnabled);

//    /**
//     * Finds a User by ID, eagerly fetching the collections needed for the PATCH DTO
//     * (electronicAddresses, roles, and address). This avoids the N+1 problem
//     * when accessing these lazy collections later in the service layer.
//     * * @param userId The ID of the User to fetch.
//     * @return An Optional containing the eagerly loaded User entity.
//     */
//    @EntityGraph(attributePaths = {"electronicAddresses", "roles", "address"})
//    Optional<User> findByIdWithGraphs(String userId);

}