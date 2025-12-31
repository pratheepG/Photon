package com.photon.identity.onboarding.repository;

import com.photon.identity.onboarding.entity.FormStructureMap;
import com.photon.identity.onboarding.enums.StructureItemType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormStructureMapRepository extends JpaRepository<FormStructureMap, Long> {
    List<FormStructureMap>  findByFormIdOrderById(Long formId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FormStructureMap f SET f.required = :required, f.dtoFieldName = :dtoFieldName WHERE f.form.id = :formId AND f.referenceId = :referenceId AND f.type = :type")
    int updateStructureDetails(Long formId, Long referenceId, boolean required, String dtoFieldName, StructureItemType type);

    boolean existsByFormIdAndReferenceIdAndType(Long formId, Long referenceId, StructureItemType type);

}