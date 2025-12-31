package com.photon.identity.authentication.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.photon.identity.commons.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "address")
public class Address implements Serializable{

	@Serial
	private static final long serialVersionUID = 6307629755781546764L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private AddressType type;

	private String streetName;
	private String houseNumber;
	private String pin;
	private String district;
	private String districtId;
	private String state;
	private String stateId;
	private String stateCode;
	private String city;
	private String cityId;
	private boolean isPrimary;

	@CreatedDate
	@Column(name = "created_date")
	private Instant createdDate;

	@LastModifiedDate
	@Column(name = "last_modified_date")
	private Instant lastModifiedDate;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@ToString.Exclude
	@JsonBackReference
	private User user;

	@Override
	public int hashCode() {
		return Objects.hash(id, streetName, houseNumber, city, pin, district, state, isPrimary);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Address address = (Address) o;
		return isPrimary == address.isPrimary && Objects.equals(id, address.id) && Objects.equals(streetName, address.streetName) && Objects.equals(houseNumber, address.houseNumber) && Objects.equals(city, address.city) && Objects.equals(pin, address.pin) && Objects.equals(district, address.district) && Objects.equals(state, address.state);
	}
	
}