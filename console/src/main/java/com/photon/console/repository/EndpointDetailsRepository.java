package com.photon.console.repository;

import com.photon.console.entity.EndpointDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EndpointDetailsRepository extends JpaRepository<EndpointDetails, String> {

    @Query("select e.id from EndpointDetails e")
    Page<String> findAllIds(Pageable pageable);

    @Query("select e.id from EndpointDetails e")
    List<String> findAllIds();

    @Query("select distinct e from EndpointDetails e " +
            "left join fetch e.models m " +
            "left join fetch e.features f " +
            "left join fetch f.actions a " +
            "where e.id = :id")
    Optional<EndpointDetails> findByIdWithAssociations(@Param("id") String id);

    @Query("select e from EndpointDetails e left join fetch e.models m where e.id = :id")
    Optional<EndpointDetails> findWithModels(@Param("id") String id);

    @Query("select e from EndpointDetails e " +
            "left join fetch e.features f " +
            "left join fetch f.actions a " +
            "where e.id = :id")
    Optional<EndpointDetails> findWithFeaturesAndActions(@Param("id") String id);

}