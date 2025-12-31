package com.photon.properties;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class AlertEventConfigProperties {

    private final Environment environment;
    private final ApplicationConfigProperties appConfig;

    public AlertEventConfigProperties(Environment environment, ApplicationConfigProperties appConfig) {
        this.environment = environment;
        this.appConfig = appConfig;
    }

    public FeatureBuilder feature(String feature) {
        return new FeatureBuilder(feature);
    }

    public class FeatureBuilder {
        private final String feature;

        private FeatureBuilder(String feature) {
            this.feature = feature;
        }

        public ActionBuilder action(String action) {
            return new ActionBuilder(this.feature, action);
        }
    }

    public class ActionBuilder {
        private final String feature;
        private final String action;

        private ActionBuilder(String feature, String action) {
            this.feature = feature;
            this.action = action;
        }

        public EventBuilder event(String event) {
            return new EventBuilder(this.feature, this.action, event);
        }
    }

    public class EventBuilder {
        private final String feature;
        private final String action;
        private final String event;

        private EventBuilder(String feature, String action, String event) {
            this.feature = feature;
            this.action = action;
            this.event = event;
        }

        public boolean isEnabled() {
            String key = buildKey();
            String value = environment.getProperty(key);
            return StringUtils.hasText(value) && value.equalsIgnoreCase("true");
        }

        private String buildKey() {
            return "photon.".concat(appConfig.getApplicationName().toLowerCase()).concat(".alert.event")
                    + "." + feature.toLowerCase()
                    + "." + action.toLowerCase()
                    + "." + event;
        }

        @Override
        public String toString() {
            return buildKey();
        }
    }
}