-- Create subscriber Table
CREATE TABLE subscriber (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_name VARCHAR(255),
    user_id VARCHAR(100) NOT NULL,
    unique_id VARCHAR(100) NOT NULL UNIQUE,
    subscriber_name VARCHAR(255),
    subscriber_unique_id VARCHAR(100) UNIQUE,
    email VARCHAR(255),
    phone_number VARCHAR(50),
    country_code VARCHAR(10),
    subscriber_status VARCHAR(50) NOT NULL,
    device_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);