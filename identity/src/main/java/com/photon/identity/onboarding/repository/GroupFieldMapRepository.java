package com.photon.identity.onboarding.repository;

import com.photon.identity.onboarding.entity.GroupFieldMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupFieldMapRepository extends JpaRepository<GroupFieldMap, Long> {
    List<GroupFieldMap> findByGroupId(Long id);
    void deleteByGroupId(Long id);

    @Modifying
    @Query("UPDATE GroupFieldMap g SET g.isRequired = :required WHERE g.group.id = :groupId AND g.field.id = :fieldId")
    void updateRequiredStatus(@Param("groupId") Long groupId, @Param("fieldId") Long fieldId, @Param("required") boolean required);

    boolean existsByGroupIdAndFieldId(Long groupId, Long fieldId);

    @Modifying
    @Query("UPDATE GroupFieldMap g SET g.isRequired = :required, g.dtoFieldName = :dtoFieldName WHERE g.group.id = :groupId AND g.field.id = :fieldId")
    void updateGroupFieldDetails(Long groupId, Long fieldId, boolean required, String dtoFieldName);

}