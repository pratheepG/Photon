-- Create dead_letter_alert Table
CREATE TABLE dead_letter_alert (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(100) NOT NULL,
    alert_type VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    alert_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    manually_processed BOOLEAN DEFAULT FALSE
);