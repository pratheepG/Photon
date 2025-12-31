package com.photon.identity.authentication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

	@Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq")
    @SequenceGenerator(name = "refresh_token_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(unique = true)
    private UserDevice userDevice;

    private Long refreshCount;

    @Column(nullable = false)
    private Instant expiryDate;
    
    public void incrementRefreshCount() {
        refreshCount = refreshCount + 1;
    }

    public RefreshToken(Long id, String token, UserDevice userDevice, Long refreshCount, Instant expiryDate) {
		super();
		this.id = id;
		this.token = token;
		this.userDevice = userDevice;
		this.refreshCount = refreshCount;
		this.expiryDate = expiryDate;
	}
    
	public RefreshToken() {}
	
}
