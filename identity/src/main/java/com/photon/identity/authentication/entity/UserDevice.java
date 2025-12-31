package com.photon.identity.authentication.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author pratheepg
 */
@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_device")
public class UserDevice {

	@Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_device_seq")
    @SequenceGenerator(name = "user_device_seq", allocationSize = 1)
    private Long id;
    private String userId;
    private String deviceType;
    @Column(unique = true)
    private String deviceId;
    private Boolean isActive;
    @OneToOne(mappedBy = "userDevice", orphanRemoval = true)
    private RefreshToken refreshToken;
    private Boolean isRefreshActive;
    @CreatedDate
    private Date createdOn;
    @LastModifiedDate
    private Date modifiedOn;
	

	public UserDevice(Long id, String userId, String deviceType, String deviceId, Boolean isActive, RefreshToken refreshToken, Boolean isRefreshActive, Date createdOn, Date modifiedOn, String regId) {
		super();
		this.id = id;
		this.userId = userId;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.isActive = isActive;
		this.refreshToken = refreshToken;
		this.isRefreshActive = isRefreshActive;
		this.createdOn = createdOn;
		this.modifiedOn = modifiedOn;
	}

	@Override
	public String toString() {
		return "UserDevice [id=" + id + ", userId=" + userId + ", deviceType=" + deviceType + ", deviceId=" + deviceId
				+ ", isActive=" + isActive + ", refreshToken=" + refreshToken + ", isRefreshActive=" + isRefreshActive
				+ ", createdOn=" + createdOn + ", modifiedOn=" + modifiedOn + "]";
	}
    
}