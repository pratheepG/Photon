package com.photon.identity.commons.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author pratheepg
 */
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cdn_asset_info")
public class CdnAssetInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String metaId;
    private String url;
}