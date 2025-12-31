-- Create UUID Generator
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create alert_template Table
CREATE TABLE alert_template (
    id BIGSERIAL PRIMARY KEY,
    template_id VARCHAR(100) UNIQUE NOT NULL,
    alert_id BIGINT,
    channel VARCHAR(50) NOT NULL,
    subject_template TEXT,
    message_template TEXT,
    sms_deeplink_url TEXT,
    sms_media_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);