-- Create subscription_topic Table
CREATE TABLE subscription_topic (
    id UUID PRIMARY KEY,
    topic VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);