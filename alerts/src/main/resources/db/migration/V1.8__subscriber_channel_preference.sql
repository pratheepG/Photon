-- Create subscriber_channel_preference Table
CREATE TABLE subscriber_channel_preference (
    id UUID PRIMARY KEY,
    subscriber_id UUID REFERENCES subscriber(id) ON DELETE CASCADE,
    channel VARCHAR(50),
    is_subscribed BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);