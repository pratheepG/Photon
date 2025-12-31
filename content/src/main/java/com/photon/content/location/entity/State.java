package com.photon.content.location.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("state")
public class State {
    @Id
    private Long id;
    private String name;
    private String code;
}