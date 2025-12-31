package com.photon.content.location.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

@Data
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("city")
public class City {
    @Id
    private Long id;
    private String name;
    private String pinCode;
    private String stateCode;
    private Long districtId;
}