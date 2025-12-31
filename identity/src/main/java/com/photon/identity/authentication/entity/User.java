package com.photon.identity.authentication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.photon.identity.authentication.utils.UserIdGenerator;
import com.photon.identity.commons.entity.CdnAssetInfo;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@Table(name = "\"user\"", indexes = {
		@Index(name = "idx_user_email", columnList = "email"),
		@Index(name = "idx_user_phone_number", columnList = "phone_number"),
		@Index(name = "idx_user_user_name", columnList = "user_name")
})
public class User implements UserDetails{

	@Serial
	private static final long serialVersionUID = 7941680041245636906L;
	
	@Id
	@GenericGenerator(name = "sequence_user_id", type = UserIdGenerator.class)
	@GeneratedValue(generator = "sequence_user_id")
	private String userId;

	@Version
	@Column(name = "version", nullable = false)
	private Long version = 0L;

	private String subscriberId;
	private String firstName;
	private String lastName;

	@Column(unique = true)
	private String userName;

	@Column(unique = true)
	private String phoneNumber;

	private String countryCode;

	@Column(unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	private Gender sex;

	private Boolean isEnabled;
	private Boolean isAccountNonExpired;
	private Boolean isAccountNonLocked;
	private Boolean isCredentialsNonExpired;
	private int inValidLoginAttempts;
	private String inValidLoginAttemptAuthType;
	@JsonIgnore
	private String password;
	@Past
	private Date dob;

	@CreatedDate
    private Date createdOn;

	@LastModifiedDate
	private Date updatedOn;

	private Date lastMfaVerifiedOn;
	private Date lastPasswordUpdatedOn;
	private Boolean isServerConsoleUser = false;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_tenant", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "tenant_id"))
	private Set<Tenant> tenants = new HashSet<>();

	@Column(nullable = false)
	private Boolean isMfaEnabled = false;

	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

	@ToString.Exclude
	@JsonManagedReference
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Address> address = new LinkedHashSet<>();

	@ToString.Exclude
	@JsonManagedReference
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ElectronicAddress> electronicAddress = new LinkedHashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "auth_type", length = 40)
	private Set<AuthAdaptor> activeAuthAdapters = new LinkedHashSet<>();

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "profile_pic_id", referencedColumnName = "id")
	private CdnAssetInfo profilePic;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> additionalAttributes = new HashMap<>();

	@Override
	public boolean isEnabled() {
	    return isEnabled;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
	}
	
	@Override
	public String getUsername() {
		return userName;
	}

	@Override
	public boolean isAccountNonExpired() {
		return isAccountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return isAccountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return isCredentialsNonExpired;
	}
	
	@Override
	public String getPassword() {
		return password;
	}


	public User() {
		super();
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
		Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
		if (thisEffectiveClass != oEffectiveClass) return false;
		User user = (User) o;
		return getUserId() != null && Objects.equals(getUserId(), user.getUserId());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getUserId());
	}
}