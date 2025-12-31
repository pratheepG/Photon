package com.photon.alerts.dto;

import com.photon.alerts.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTemplateDto {

    private String id;
    private Channel channel;
    private String subjectTemplate;
    private String messageTemplate;
    private String smsDeeplinkUrl;
    private String smsMediaUrl;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private List<RemoteAttachmentDto> mailAttachments;
}
