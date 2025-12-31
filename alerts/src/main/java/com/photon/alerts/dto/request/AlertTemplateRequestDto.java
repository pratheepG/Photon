package com.photon.alerts.dto.request;

import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTemplateRequestDto {
    private Channel channel;
    private String subjectTemplate;
    private String messageTemplate;
    private String smsDeeplinkUrl;
    private String smsMediaUrl;
    private List<RemoteAttachmentDto> mailAttachments;
}