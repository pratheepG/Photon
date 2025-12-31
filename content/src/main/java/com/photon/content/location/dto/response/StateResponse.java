package com.photon.content.location.dto.response;

import lombok.*;

@Data
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class StateResponse {
    private Long id;
    private String name;
    private String code;
}