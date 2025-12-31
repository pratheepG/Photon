package com.photon.alerts.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    private boolean isHtml;

    private List<RemoteAttachmentDto> attachments;
}