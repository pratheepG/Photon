-- Create device_info Table
CREATE TABLE device_info (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_type VARCHAR(50) NOT NULL,         -- e.g., 'ANDROID', 'IOS', 'WEB', 'DESKTOP'
    device_id VARCHAR(255) NOT NULL,          -- Unique device identifier
    is_active BOOLEAN DEFAULT TRUE,
    reg_id TEXT,                              -- Registration ID for push notifications
    platform VARCHAR(50),                     -- e.g., 'Chrome', 'Safari', 'Firefox' for WEB
    os_version VARCHAR(50),                   -- e.g., 'Android 13', 'iOS 16.4'
    app_version VARCHAR(50),                  -- Application version
    device_model VARCHAR(100),                -- e.g., 'iPhone 14 Pro', 'Samsung Galaxy S23'
    device_brand VARCHAR(100),                -- e.g., 'Apple', 'Samsung', 'Google'
    browser_version VARCHAR(100),             -- For web devices
    screen_resolution VARCHAR(50),            -- e.g., '1920x1080'
    ip_address VARCHAR(45),                   -- Supports both IPv4 and IPv6
    user_agent TEXT,                          -- Full user agent string
    last_active TIMESTAMP,                    -- Last active timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_device UNIQUE(device_id, device_type)
);