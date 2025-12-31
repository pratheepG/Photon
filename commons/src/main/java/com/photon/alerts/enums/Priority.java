package com.photon.alerts.enums;

import lombok.Getter;

@Getter
public enum Priority {
    HIGH("high-priority-alerts"),
    MEDIUM_HIGH("medium-high-priority-alerts"),
    MEDIUM("medium-priority-alerts"),
    MEDIUM_LOW("medium-low-priority-alerts"),
    LOW("low-priority-alerts");

    final String brokerTopic;

    Priority(String brokerTopic) {
        this.brokerTopic = brokerTopic;
    }

}