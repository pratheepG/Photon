package com.photon.console.identity.entity;

import com.photon.console.identity.entity.converter.FeatureActionsConverter;
import com.photon.endpoint.enums.AccessLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author pratheepg
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "role", uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "idp"}),
		indexes = {@Index(name = "idx_role_id", columnList = "role_id")})
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "role_id", length = 60, nullable = false)
	private String roleId;

	@Column(name = "name", length = 60)
	private String name;

	@Column(name = "description", length = 256)
	private String description;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Enumerated(EnumType.STRING)
	@Column(name = "access_level")
	private AccessLevel accessLevel;

	@Column(name = "idp", length = 60)
	private String idp;

//	@CreatedDate
//	@Column(nullable = false, updatable = false)
//	private Date createdOn;
//
//	@LastModifiedDate
//	@Column(nullable = true, updatable = true)
//	private Date modifiedOn;
//
//	@CreatedBy
//	@Column(updatable = false, nullable = false)
//	private String createdBy;
//
//	@LastModifiedBy
//	@Column(nullable = true, updatable = true)
//	private String modifiedBy;

	@Convert(converter = FeatureActionsConverter.class)
	@Column(name = "feature_actions", columnDefinition = "TEXT")
	private Map<String, Set<String>> featureActions = new LinkedHashMap<>();

}