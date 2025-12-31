-- Create alert Table
CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(255) NOT NULL,
    alert_sub_type VARCHAR(255),
    audience VARCHAR(50),
    topic VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Composite unique constraint
    CONSTRAINT uq_alert_type_sub_type UNIQUE (alert_type, alert_sub_type)
);