package com.photon.dto;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportingParamsDto {
    private String iP;
    private String applicationId;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String deviceOs;
    private String osVersion;
    private String userAgent;

    public String getBrowserVersion() {
        if (userAgent == null || userAgent.isEmpty()) {
            return "UNKNOWN";
        }

        if (userAgent.contains("Chrome/")) {
            return getVersion(userAgent, "Chrome/");
        }

        if (userAgent.contains("Firefox/")) {
            return getVersion(userAgent, "Firefox/");
        }

        if (userAgent.contains("Edg/")) {
            return getVersion(userAgent, "Edg/");
        }

        if (userAgent.contains("Safari/") && userAgent.contains("Version/")) {
            return getVersion(userAgent, "Version/");
        }

        return "UNKNOWN";
    }

    private static String getVersion(String userAgent, String keyword) {
        int index = userAgent.indexOf(keyword);
        if (index != -1) {
            int start = index + keyword.length();
            int end = userAgent.indexOf(" ", start);
            return userAgent.substring(start, end != -1 ? end : userAgent.length());
        }
        return "UNKNOWN";
    }
}