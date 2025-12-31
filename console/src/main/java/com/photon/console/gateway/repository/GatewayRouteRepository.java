package com.photon.console.gateway.repository;

import com.photon.console.gateway.entity.GatewayRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, Long> {

    Optional<GatewayRoute> findByRouteId(String rId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM GatewayRoute r WHERE r.routeId = :routeId")
    void deleteByRouteId(String routeId);

}