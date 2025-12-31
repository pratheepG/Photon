package com.photon.content.location.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

@Data
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("district")
public class District {
    @Id
    private Long id;
    private String name;
    private Long stateId;
    private String stateCode;
}